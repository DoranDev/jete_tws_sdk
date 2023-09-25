//
//  OTAManager.swift
//  AB OTA Demo
//
//  Created by Bluetrum on 2020/12/25.
//

import Foundation
import CoreBluetooth


public protocol OtaRequestDelegate: AnyObject {
    func sendRequest(_ request: Request, requestCompletion: RequestCompletion?)
}

/// `OtaManager`代理，用于监听OTA事件。
public protocol OtaManagerDelegate: AnyObject {
    /// 通知设备是否允许升级
    func onOtaAllowUpdate(_ allowed: Bool)
    /// OTA已开始
    func onOtaStart()
    /**
     OTA进度
     - Parameters:
        - progress: 进度，[0, 1.0]
     */
    func onOtaProgress(_ progress: Float)
    /// OTA已停止
    func onOtaStop()
    /// OTA已全部完成
    func onOtaFinish()
    /// OTA已暂停
    func onOtaPause()
    /// OTA已继续
    func onOtaContinue()
    /**
     OTA遇到错误
     - Parameters:
        - error: 错误，`OTAError`
     */
    func onOtaError(_ error: OTAError)
}

public class OtaManager: NSObject {
    
    /// 发送每一个数据块之后，等待固件回复的默认超时间隔
    public static let DEFAULT_TIMEOUT: Int = 10 // In second, 10s
    /// 固件默认版本
    public static let UNDEFINED_FIRMWARE_VERSION: UInt32 = 0xFFFFFFFF
    
    public weak var requestDelegate: OtaRequestDelegate?
    
    // MARK: - Properties
    
    /// 代理`OtaManagerDelegate`用以接收OTA状态
    public weak var delegate: OtaManagerDelegate?
    /// OTA文件的版本，默认是0xFFFF，即接受全部版本
    public var firmwareVersion: UInt32 = UNDEFINED_FIRMWARE_VERSION
    public var hashData: Data = Data([0xFF, 0xFF, 0xFF, 0xFF])
    
    weak var logger: LoggerDelegate? = DefaultLogger.shared
    
    var dataProvider: OtaDataProvider?
    var requestGenerator: OtaRequestGenerator
    var allowedUpdate: Bool = false
    
    private var blockSize = OtaConstants.DEFAULT_BLOCK_SIZE {
        didSet {
            dataProvider?.blockSize = blockSize
        }
    }
    public var packetSize = OtaConstants.DEFAULT_PACKET_SIZE {
        didSet {
            dataProvider?.packetSize = packetSize
        }
    }
    
    private var isUpdatePause: Bool = false

    public var isUpdating: Bool = false
    
    var disconnectedDueToDeviceError: Bool = false // 报错后断开不再调用onStop, FIXME: Keep it?
    
    private var timeoutCallback: DispatchWorkItem?

    // MARK: - Public API
    
    /// 构造器
    public override init() {
        requestGenerator = OtaRequestGenerator(dataProvider: nil)
        super.init()
    }
    
    /// OTA Manager初始化，在进行OTA升级之前必须进行初始化。
    public func initialize() {
    }
    
    /**
     释放资源，包括断开蓝牙连接。
     通常在升级完成、遇到错误，或者超时的时候，会自动释放资源，但是如果需要中断升级，亦可手动调用。
     */
    public func deinitialize() {
        firmwareVersion = OtaManager.UNDEFINED_FIRMWARE_VERSION
        allowedUpdate = false
        isUpdating = false
        isUpdatePause = false
        cancelTimeout()
        dataProvider?.reset() // 在蓝牙断开的地方调用
    }
    
    public func prepareToUpdate() {
        if dataProvider != nil {
            requireOtaInfo()
        }
    }
    
    /// 开始进行OTA升级
    public func startOTA() {
        if isReadyToUpdate() {
            isUpdating = true
            notifyOnStart()
            sendBlock()
        }
    }
    
    /**
     判断是否已经就绪。
     
     - Returns: 是否已就绪
     */
    public func isReadyToUpdate() -> Bool {
        return dataProvider != nil && allowedUpdate
    }
    
    /**
     设置OTA数据。
     
     - Parameters:
        - otaData: OTA数据
     */
    public func setOtaData(_ otaData: Data) {
        dataProvider = OtaDataProvider(otaData: otaData)
        requestGenerator.dataProvider = dataProvider
        
        hashData = getDataHash(data: otaData)
    }
    
    // MARK: - Notify
    
    private func notifyAllowUpdate(_ allowed: Bool) {
        DispatchQueue.main.async {
            self.delegate?.onOtaAllowUpdate(allowed)
        }
    }
    
    private func notifyOnStart() {
        DispatchQueue.main.async {
            self.delegate?.onOtaStart()
        }
    }
    
    private func notifyOnProgress(_ progress: UInt32) {
        DispatchQueue.main.async {
            self.delegate?.onOtaProgress(Float(progress))
        }
    }
    
    private func notifyOnFinish() {
        DispatchQueue.main.async {
            self.delegate?.onOtaFinish()
        }
    }
    
    private func notifyOnStop() {
        if !disconnectedDueToDeviceError {
            DispatchQueue.main.async {
                self.delegate?.onOtaStop()
            }
        }
        disconnectedDueToDeviceError = false
    }
    
    private func notifyOnPause() {
        DispatchQueue.main.async {
            self.delegate?.onOtaPause()
        }
    }
    
    private func notifyOnContinue() {
        DispatchQueue.main.async {
            self.delegate?.onOtaContinue()
        }
    }
    
    private func notifyOnError(_ error: OTAError) {
        disconnectedDueToDeviceError = true
        DispatchQueue.main.async {
            self.delegate?.onOtaError(error)
        }
        deinitialize()
    }
    
    private func requireOtaInfo() {
        guard let dataProvider = dataProvider else {
            return
        }
        
        let infoRequest = OtaInfoRequest(version: firmwareVersion, hashData: hashData, dataSize: dataProvider.dataSize)
        sendRequest(infoRequest) { [weak self] request, result, timeout in
            
            if timeout {
                self?.notifyOnError(.timeoutWaitingResponse)
            } else {
                if case let otaInfo? = result, let info = otaInfo as? OtaInfo {
                    print("otaInfo: \(String(describing: info))")
                    
                    self?.dataProvider?.startAddress = info.startAddress
                    self?.dataProvider?.blockSize = info.blockSize
                    self?.allowedUpdate = info.allowUpdate
                    
                    // 如果是从暂停中恢复，自动开始升级流程
                    if self != nil, self!.isUpdatePause {
                        self?.isUpdatePause = false
                        self?.startOTA()
                    } else {
                        self?.notifyAllowUpdate(info.allowUpdate)
                    }
                }
            }
        }
    }
    
    // MARK: -
    
    private func canSendNow() -> Bool {
        if let dataProvider = dataProvider {
            return !dataProvider.isBlockSentFinish()
        }
        return false
    }
    
    // MARK: - Request
    
    private func sendOtaStart() {
        if let dataProvider = dataProvider {
            let request = requestGenerator.getOtaStartRequest()
            sendRequest(request)
            
            // Report progress
            let progress = dataProvider.progress
            notifyOnProgress(progress)
        }
    }
    
    private func sendOtaData() {
        if let dataProvider = dataProvider {
            let request = requestGenerator.getOtaSendDataRequest()
            sendRequest(request)
            
            // Report progress
            let progress = dataProvider.progress
            notifyOnProgress(progress)
        }
    }
    
    private func doSendData() {
        // Continue sending data
        while canSendNow() {
            sendOtaData()
        }
        // 发完块的最后一包后，就等待固件回复
        if let isBlockSentFinish = dataProvider?.isBlockSentFinish(), isBlockSentFinish {
            timeoutCallback?.cancel()
            timeoutCallback = DispatchWorkItem { [weak self] in
                self?.handleTimeout()
            }
            DispatchQueue.global().asyncAfter(deadline: .now() + .seconds(OtaManager.DEFAULT_TIMEOUT), execute: timeoutCallback!)
        }
    }
    
    private func sendBlock() {
        sendOtaStart()
        doSendData()
    }
    
    private func handleTimeout() {
       // logger?.e(.otaManager, "Waiting for response timeout")
        notifyOnError(OTAError.timeoutWaitingResponse)
    }
    
    private func cancelTimeout() {
        timeoutCallback?.cancel()
        timeoutCallback = nil
    }
    
    private func getDataHash(data: Data) -> Data {
        return data.md5().subdata(in: 0..<4)
    }
    
}

extension OtaManager {
    
    private func sendRequest(_ request: Request) {
        sendRequest(request, requestCompletion: nil)
    }
    
    private func sendRequest(_ request: Request, requestCompletion: RequestCompletion?) {
        requestDelegate?.sendRequest(request, requestCompletion: requestCompletion)
    }
    
}

extension OtaManager {
    
    public func updateOTAState(state: UInt8) {
        
        if let otaState = OtaState(rawValue: state) {
            switch otaState {
            case .STATE_OK:
                cancelTimeout()
                // If not in pause state, and not done yet, go on sending, until done
                if !isUpdatePause, let dataProvider = dataProvider, !dataProvider.isAllDataSent() {
                    sendBlock()
                }
            case .STATE_DONE:
                cancelTimeout()
                isUpdating = false
                notifyOnFinish()
            case .STATE_PAUSE:
                cancelTimeout()
                // Pause
                allowedUpdate = false
                isUpdating = false
                isUpdatePause = true
                notifyOnPause()
            case .STATE_CONTINUE:
                // Resume
                notifyOnContinue()
                // Continue to update
                prepareToUpdate()
            }
        } else {
            isUpdating = false
            cancelTimeout()
            notifyOnError(.deviceReport(code: state))
        }
    }
    
}
