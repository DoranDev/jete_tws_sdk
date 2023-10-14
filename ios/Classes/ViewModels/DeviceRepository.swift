//
//  DeviceRepository.swift
//  ABMateDemo
//
//  Created by Bluetrum.
//  

import Foundation
import CoreBluetooth
import RxRelay

class DeviceRepository: NSObject {
    
    weak var logger: LoggerDelegate? = DefaultLogger.shared
    
    static let shared = DeviceRepository()
    
    // MARK: - Properties
    
    private let bluetoothManager = BluetoothManager.shared
    
    private var workingDevice: ABDevice?
    
    private var isOpened: Bool = false
    private var isScanning: Bool = false
    
    private(set) var deviceCommManager = DefaultDeviceCommManager()
    private var preparingDevice: ABDevice?
    private var waitingDeviceReady: Bool = false
    
    private var _latestDiscoveredDevice: ABDevice? {
        didSet {
            latestDiscoveredDevice.accept(_latestDiscoveredDevice)
        }
    }
    let latestDiscoveredDevice: BehaviorRelay<ABDevice?> = BehaviorRelay(value: nil)
    
    private var _discoveredDevices = [ABDevice]() {
        didSet {
            discoveredDevices.accept(_discoveredDevices)
        }
    }
    let discoveredDevices: BehaviorRelay<[ABDevice]> = BehaviorRelay(value: [])
    
    private var _activeDevice: ABDevice? {
        didSet {
            activeDevice.accept(_activeDevice)
        }
    }
    let activeDevice: BehaviorRelay<ABDevice?> = BehaviorRelay(value: nil)
    
    var devicePower: BehaviorRelay<DevicePower?> { deviceCommManager.devicePower }
    var deviceFirmwareVersion: BehaviorRelay<UInt32?> { deviceCommManager.deviceFirmwareVersion }
    var deviceName: BehaviorRelay<String?> { deviceCommManager.deviceName }
    var deviceEqSetting: BehaviorRelay<RemoteEqSetting?> { deviceCommManager.deviceEqSetting }
    var deviceKeySettings: BehaviorRelay<[KeyType: KeyFunction]?> { deviceCommManager.deviceKeySettings }
    var deviceVolume: BehaviorRelay<UInt8?> { deviceCommManager.deviceVolume }
    var devicePlayState: BehaviorRelay<Bool?> { deviceCommManager.devicePlayState }
    var deviceWorkMode: BehaviorRelay<UInt8?> { deviceCommManager.deviceWorkMode }
    var deviceInEarStatus: BehaviorRelay<Bool?> { deviceCommManager.deviceInEarStatus }
    var deviceLanguageSetting: BehaviorRelay<UInt8?> { deviceCommManager.deviceLanguageSetting }
    var deviceAutoAnswer: BehaviorRelay<Bool?> { deviceCommManager.deviceAutoAnswer }
    var deviceAncMode: BehaviorRelay<UInt8?> { deviceCommManager.deviceAncMode }
    var deviceIsTws: BehaviorRelay<Bool?> { deviceCommManager.deviceIsTws }
    var deviceTwsConnected: BehaviorRelay<Bool?> { deviceCommManager.deviceTwsConnected }
    var deviceLedSwitch: BehaviorRelay<Bool?> { deviceCommManager.deviceLedSwitch }
    var deviceFwChecksum: BehaviorRelay<Data?> { deviceCommManager.deviceFwChecksum }
    var deviceAncGain: BehaviorRelay<Int?> { deviceCommManager.deviceAncGain }
    var deviceTransparencyGain: BehaviorRelay<Int?> { deviceCommManager.deviceTransparencyGain }
    var deviceAncGainNum: BehaviorRelay<Int?> { deviceCommManager.deviceAncGainNum }
    var deviceTransparencyGainNum: BehaviorRelay<Int?> { deviceCommManager.deviceTransparencyGainNum }
    var deviceRemoteEqSettings: BehaviorRelay<[RemoteEqSetting]?> { deviceCommManager.deviceRemoteEqSettings }
    var deviceLeftIsMainSide: BehaviorRelay<Bool?> { deviceCommManager.deviceLeftIsMainSide }
    var deviceProductColor: BehaviorRelay<UInt8?> { deviceCommManager.deviceProductColor }
    var deviceSoundEffect3d: BehaviorRelay<Bool?> { deviceCommManager.deviceSoundEffect3d }
    var deviceCapacities: BehaviorRelay<DeviceCapacities?> { deviceCommManager.deviceCapacities }
    
    let deviceMaxPacketSize: BehaviorRelay<UInt16?> = BehaviorRelay(value: nil)
    
    // MARK: - Public API
    
    public override init() {
        super.init()
        self.bluetoothManager.delegate = self
    }
    
    func startScanning() {
        guard !isScanning else { return }
        
       // resetPeripherals()
        isScanning = true
        bluetoothManager.startScanning()
    }
    
    func stopScanning() {
        guard isScanning else { return }
        
        bluetoothManager.stopScanning()
        isScanning = false
    }
    
    func connect(_ device: ABDevice) {
        workingDevice = device
        
        device.connectionStateCallback = self
        device.dataDelegate = self
        preparingDevice = device

        var options: [String: Any] = [:]
        if #available(iOS 13.2, *) {
            options[CBConnectPeripheralOptionEnableTransportBridgingKey] = true
        }
        bluetoothManager.connect(device.peripheral, options: options)
        isOpened = true
    }
    
    func disconnect() {
        guard let device = workingDevice else {
            return
        }
        print("disconnect")
        _activeDevice?.stop()
        bluetoothManager.disconnect(device.peripheral)
        isOpened = false
    }
    
    func sendRequest(_ request: Request) {
        sendRequest(request, completion: nil)
    }
    
    func sendRequest(_ request: Request, completion: RequestCompletion?) {
        guard let device = workingDevice, device.peripheral.state == .connected else {
            return
        }
        deviceCommManager.sendRequest(request, completion: completion)
    }
    
    func resetDeviceStatus() {
        _latestDiscoveredDevice = nil
        _discoveredDevices = []
        
        deviceCommManager.resetDeviceStatus()
        deviceMaxPacketSize.accept(nil)
    }
    
    // MARK: - Implementation
    
    private func discoverDevice(_ device: ABDevice) {
//        logger?.v(.deviceRepository, "Found device: \(device)")
        
        _discoveredDevices.append(device)
        _latestDiscoveredDevice = device
    }
    
    private func resetPeripherals() {
        _latestDiscoveredDevice = nil
        _discoveredDevices.removeAll()
    }
    
    private func onDisconnected() {
        // 释放掉正在认证的设备
        if let device = preparingDevice {
            device.connectionStateCallback = nil
            device.dataDelegate = nil
            device.stop()
            preparingDevice = nil
        }
        
        // 释放当前活跃设备
        if let activeDevice = _activeDevice {
            activeDevice.connectionStateCallback = nil
            activeDevice.dataDelegate = nil
            activeDevice.stop()
            _activeDevice = nil
        }
        
        deviceCommManager.commDelegate = nil
        deviceCommManager.responseErrorHandler = nil
        deviceCommManager.reset()
        // 重置设备状态
        resetDeviceStatus()
    }
    
}

// MARK: - BluetoothDelegate

extension DeviceRepository: BluetoothDelegate {
    
    func didUpdateState(_ state: CBManagerState) {
        
        if state == .poweredOn {
            // TODO: move to another place
            // Listen bluetooth connection events
            if #available(iOS 13.0, *) {
                let ABMateServiceUUID = CBUUID(string: "FDB3")
                self.bluetoothManager.registerForConnectionEvents(options: [.serviceUUIDs: ABMateServiceUUID]) // TODO: options未定，预留
            }
            
            if isOpened {
                connect(workingDevice!)
            }
            if isScanning {
                startScanning()
            }
        } else if state == .poweredOff {
            isOpened = false
            isScanning = false
        }
    }
    
    func didStopScanning() {
        isScanning = false
    }
    

    func didDiscoverPeripheral(_ peripheral: CBPeripheral, advertisementData: [String : Any], RSSI: NSNumber) {

        if let manufacturerData = advertisementData.manufacturerData(companyId: GlobalConfig.MANUFACTURER_ID),
           let deviceBeacon = DeviceBeacon.getDeviceBeacon(data: manufacturerData),
           deviceBeacon.brandId == GlobalConfig.BRAND_ID >> 16 {

//            logger?.v(.deviceRepository, "\(deviceBeacon)")

            // 如果是耳机广播
            if let earbudsBeacon = deviceBeacon as? EarbudsBeacon {
                // 如果广播里包含的地址和当前连接的音频设备相同（理所当然isConnected==true）
                // 或者未连接
              //  if (earbudsBeacon.btAddress == Utils.bluetoothAudioDeviceAddress) || !earbudsBeacon.isConnected {
                    // 如果设备已经存在于列表，则更新状态
                    // 否则列表添加新设备
                    if let device = _discoveredDevices.first(where: { $0.peripheral == peripheral }) {
                        device.updateDeviceStatus(deviceBeacon: earbudsBeacon)
                        device.rssi = RSSI.intValue
                        latestDiscoveredDevice.accept(device)
                    } else if peripheral.name != nil {
                        // TODO: Define Product ID
                      //  if earbudsBeacon.productId == 1 {
                            let device = ABEarbuds(peripheral: peripheral, earbudsBeacon: earbudsBeacon)
                            device.rssi = RSSI.intValue
                            discoverDevice(device)
                      //  }
                    }
              //  }
            }
        }
    }

    func didConnectedPeripheral(_ connectedPeripheral: CBPeripheral) {

        if connectedPeripheral == workingDevice?.peripheral {
            logger?.i(.deviceRepository, "Connected to \(workingDevice!.name ?? "Unknown Device")")
            preparingDevice?.connect()
        }
    }

    func failToConnectPeripheral(_ peripheral: CBPeripheral, error: Error?) {

        if let error = error {
            logger?.w(.deviceRepository, error)
        } else {
            logger?.d(.deviceRepository, "Device is disconnected")
        }
    }

    func didDisconnectPeripheral(_ peripheral: CBPeripheral, error: Error?) {

        if peripheral == self.workingDevice?.peripheral {

            let deviceNotSupported = preparingDevice != nil

            if let error = error as NSError? {
                switch error.code {
                case 6, 7: logger?.e(.deviceRepository, error.localizedDescription)
                default: logger?.e(.deviceRepository, "Disconnected from \(peripheral.name ?? "Unknown Device") with error: \(error)")
                }
            } else {
                if !deviceNotSupported {
                    logger?.i(.deviceRepository, "Disconnected from \(peripheral.name ?? "Unknown Device")")
                } else {
                    logger?.e(.deviceRepository, "Disconnected from \(peripheral.name ?? "Unknown Device") with error: Device not supported")
                }
            }

            self.workingDevice = nil

            onDisconnected()
        }
    }

    func connectionEventDidOccur(_ event: CBConnectionEvent, for peripheral: CBPeripheral) {
        switch event {
        case .peerDisconnected:
            print("TODO: peerDisconnected, check peripheral")
        case .peerConnected:
            print("TODO: peerConnected, check peripheral")
        @unknown default:
            print("connectionEventDidOccur: unknown event")
        }
    }

}

// MARK: - ConnectionStateCallback

extension DeviceRepository: ConnectionStateCallback {

    func onConnected(device: ABDevice) {
        deviceCommManager.commDelegate = device
        deviceCommManager.responseErrorHandler = self

        // 先获取MTU，以便设置分包大小，然后再获取其他信息
        // First of all, require MTU, in order to set max packet size, and then require other info
        registerMaxPacketSizeCallable()
        deviceCommManager.sendRequest(DeviceInfoRequest(Command.INFO_MAX_PACKET_SIZE))
    }

    func onReceiveAuthResult(device: ABDevice, passed: Bool) {
        if passed {
            print("passed")
            _activeDevice = device

            // Require all device info
            deviceCommManager.sendRequest(DeviceInfoRequest.defaultInfoRequest) { request, result, timeout in
                guard #available(iOS 13.2, *), device.supportCTKD else { return }
                guard let result = result as? Bool, result else { return }

                device.triggerCTKD()
            }
        } else {
            print("not passed")
            device.stop()
            bluetoothManager.disconnect(device.peripheral)
        }
//        // 已经不处于认证状态
        preparingDevice = nil
    }
    
}

// MARK: - DeviceDataDelegate

extension DeviceRepository: DeviceDataDelegate {
    
    func onReceiveData(_ data: Data) {
        logger?.d(.deviceRepository, "<- 0x\(data.hex)")
        deviceCommManager.handleData(data)
    }
    
}

// MARK: - DeviceResponseErrorHandler

extension DeviceRepository: DeviceResponseErrorHandler {
    
    func onError(_ error: ResponseError) {
        logger?.d(.deviceRepository, "Device Response error: \(error)")
    }
    
}

// MARK: - DeviceCommManager

extension DeviceRepository {
    
    func registerMaxPacketSizeCallable() {
        deviceCommManager.registerDeviceInfoCallback(Command.INFO_MAX_PACKET_SIZE, callableType: MtuPayloadHandler.self) {
            [weak self] in
            if let maxPacketSize = $0 as? Int {
                self?.logger?.d(.deviceRepository, "Max Packet Size: \(maxPacketSize)")
                // No longer deal with Command.INFO_MAX_PACKET_SIZE
                self?.deviceCommManager.unregisterDeviceInfoCallback(Command.INFO_MAX_PACKET_SIZE)
                // Update Value
                self?.deviceMaxPacketSize.accept(UInt16(maxPacketSize))
                // Set max communication packet size
                self?.deviceCommManager.maxPacketSize = maxPacketSize
                // Start authentication, if the device does not require authentication, it will directly return success
                self?.preparingDevice?.startAuth()
            }
        }
    }
    
}
