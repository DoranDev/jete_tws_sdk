//
//  Defaultswift
//  DeviceManager
//
//  Created by Bluetrum on 2022/4/2.
//

import Foundation
import RxRelay

public class DefaultDeviceCommManager: DeviceCommManager {
    
    public let devicePower: BehaviorRelay<DevicePower?> = BehaviorRelay(value: nil)
    public let deviceFirmwareVersion: BehaviorRelay<UInt32?> = BehaviorRelay(value: nil)
    public let deviceName: BehaviorRelay<String?> = BehaviorRelay(value: nil)
    public let deviceEqSetting: BehaviorRelay<RemoteEqSetting?> = BehaviorRelay(value: nil)
    public let deviceKeySettings: BehaviorRelay<[KeyType: KeyFunction]?> = BehaviorRelay(value: nil)
    public let deviceVolume: BehaviorRelay<UInt8?> = BehaviorRelay(value: nil)
    public let devicePlayState: BehaviorRelay<Bool?> = BehaviorRelay(value: nil)
    public let deviceWorkMode: BehaviorRelay<UInt8?> = BehaviorRelay(value: nil)
    public let deviceInEarStatus: BehaviorRelay<Bool?> = BehaviorRelay(value: nil)
    public let deviceLanguageSetting: BehaviorRelay<UInt8?> = BehaviorRelay(value: nil)
    public let deviceAutoAnswer: BehaviorRelay<Bool?> = BehaviorRelay(value: nil)
    public let deviceAncMode: BehaviorRelay<UInt8?> = BehaviorRelay(value: nil)
    public let deviceIsTws: BehaviorRelay<Bool?> = BehaviorRelay(value: nil)
    public let deviceTwsConnected: BehaviorRelay<Bool?> = BehaviorRelay(value: nil)
    public let deviceLedSwitch: BehaviorRelay<Bool?> = BehaviorRelay(value: nil)
    public let deviceFwChecksum: BehaviorRelay<Data?> = BehaviorRelay(value: nil)
    public let deviceAncGain: BehaviorRelay<Int?> = BehaviorRelay(value: nil)
    public let deviceTransparencyGain: BehaviorRelay<Int?> = BehaviorRelay(value: nil)
    public let deviceAncGainNum: BehaviorRelay<Int?> = BehaviorRelay(value: nil)
    public let deviceTransparencyGainNum: BehaviorRelay<Int?> = BehaviorRelay(value: nil)
    public let deviceRemoteEqSettings: BehaviorRelay<[RemoteEqSetting]?> = BehaviorRelay(value: nil)
    public let deviceLeftIsMainSide: BehaviorRelay<Bool?> = BehaviorRelay(value: nil)
    public let deviceProductColor: BehaviorRelay<UInt8?> = BehaviorRelay(value: nil)
    public let deviceSoundEffect3d: BehaviorRelay<Bool?> = BehaviorRelay(value: nil)
    public let deviceCapacities: BehaviorRelay<DeviceCapacities?> = BehaviorRelay(value: nil)
    
    // Request结果（如果成功）反映到DeviceInfo
    public var enableRequestToDevInfo: Bool = true
    private var requestToDeviceInfoDict: [UInt8: UInt8] = [:]
    
    public override init() {
        super.init()
        
        registerDefaultResponseCallables()
        registerDefaultNotificationCallables()
        registerDefaultDeviceInfoCallables()
        registerRequestToDeviceInfoDicts()
    }
    
    public override func sendRequest(_ request: Request, completion: RequestCompletion?) {
        let requestCompletion: RequestCompletion = { [weak self] request, result, timeout in
            if let completion = completion {
                completion(request, result, timeout)
                // 处理完Response，再看要不要将结果反应到DevInfo
                if !timeout, let strongSelf = self, strongSelf.enableRequestToDevInfo {
                    self?.processRequestToDevInfo(request: request, result: result!)
                }
            }
        }
        super.sendRequest(request, completion: requestCompletion)
    }
    
    private func processRequestToDevInfo(request: Request, result: Any) {
        // Get map, Command -> DevInfo
        guard let infoType = requestToDeviceInfoDict[request.getCommand()] else {
            return
        }
        
        // ResponsePayloadHandler.self
        if let success = result as? Bool, success {
            processDeviceInfo(type: infoType, data: request.getPayload())
        }
        // TlvResponsePayloadHandler.self
        // Update BehaviorRelay directly, or sendRequest to get latest status
        // Here choose the first one method
        else if let tlvResponse = result as? TlvResponse {
            // Key
            if let keyRequest = request as? KeyRequest {
                let keyType = keyRequest.keyType
                let keyFunction = keyRequest.keyFunction
                if let keyResultSuccess = tlvResponse[keyType.rawValue], keyResultSuccess {
                    // Update all key settings, not only the current one
                    let newKeySetting = [keyType: keyFunction]
                    deviceKeySettingsChanged(newKeySetting)
                }
            }
            // Volume
            else if let musicControlRequest = request as? MusicControlRequest {
                let controlType = musicControlRequest.controlType
                if let controlResultSuccess = tlvResponse[controlType.rawValue], controlResultSuccess {
                    if case .volume(let v) = controlType {
                        processDeviceInfo(type: infoType, data: Data([v]))
                    }
                }
            }
        }
    }
    
    public func registerRequestToDeviceInfoDict(command: UInt8, deviceInfo: UInt8) {
        requestToDeviceInfoDict[command] = deviceInfo
    }
    
    public func unregisterRequestToDeviceInfoMap(command: UInt8) {
        requestToDeviceInfoDict[command] = nil
    }
    
    //
    
    private func deviceKeySettingsChanged(_ newKeySettings: [KeyType: KeyFunction]) {
        if var settings = deviceKeySettings.value {
            settings.merge(newKeySettings) { (_, new) in new }
            deviceKeySettings.accept(settings)
        } else {
            deviceKeySettings.accept(newKeySettings)
        }
    }
    
    /// 注册回复数据处理类
    private func registerDefaultResponseCallables() {
        
        registerResponseCallables([
            Command.COMMAND_DEVICE_INFO:        ResponsePayloadHandler.self,
            Command.COMMAND_EQ:                 ResponsePayloadHandler.self,
            Command.COMMAND_AUTO_SHUTDOWN:      ResponsePayloadHandler.self,
            Command.COMMAND_FACTORY_RESET:      ResponsePayloadHandler.self,
            Command.COMMAND_WORK_MODE:          ResponsePayloadHandler.self,
            Command.COMMAND_IN_EAR_DETECT:      ResponsePayloadHandler.self,
            Command.COMMAND_LANGUAGE:           ResponsePayloadHandler.self,
            Command.COMMAND_FIND_DEVICE:        ResponsePayloadHandler.self,
            Command.COMMAND_AUTO_ANSWER:        ResponsePayloadHandler.self,
            Command.COMMAND_ANC_MODE:           ResponsePayloadHandler.self,
            Command.COMMAND_BLUETOOTH_NAME:     ResponsePayloadHandler.self,
            Command.COMMAND_LED_MODE:           ResponsePayloadHandler.self,
            Command.COMMAND_CLEAR_PAIR_RECORD:  ResponsePayloadHandler.self,
            Command.COMMAND_ANC_GAIN:           ResponsePayloadHandler.self,
            Command.COMMAND_TRANSPARENCY_GAIN:  ResponsePayloadHandler.self,
            Command.COMMAND_SOUND_EFFECT_3D:    ResponsePayloadHandler.self,
            
            Command.COMMAND_MUSIC_CONTROL:      TlvResponsePayloadHandler.self,
            Command.COMMAND_KEY:                TlvResponsePayloadHandler.self,
        ])
    }
    
    /// 注册设备上传通知的处理类和回调
    /// callableType可以替换成自己想要的处理方法和结果
    private func registerDefaultNotificationCallables() {
        
        registerNotificationCallback(Command.NOTIFICATION_DEVICE_POWER, callableType: PowerPayloadHandler.self)           { self.devicePower.accept($0 as? DevicePower) }
        registerNotificationCallback(Command.NOTIFICATION_EQ_SETTING, callableType: RemoteEqSettingPayloadHandler.self)   { self.deviceEqSetting.accept($0 as? RemoteEqSetting) }
        registerNotificationCallback(Command.NOTIFICATION_KEY_SETTINGS, callableType: KeyPayloadHandler.self)             { self.deviceKeySettings.accept($0 as? [KeyType: KeyFunction]) }
        registerNotificationCallback(Command.NOTIFICATION_DEVICE_VOLUME, callableType: UInt8PayloadHandler.self)          { self.deviceVolume.accept($0 as? UInt8) }
        registerNotificationCallback(Command.NOTIFICATION_PLAY_STATE, callableType: BoolPayloadHandler.self)              { self.devicePlayState.accept($0 as? Bool) }
        registerNotificationCallback(Command.NOTIFICATION_WORK_MODE, callableType: UInt8PayloadHandler.self)              { self.deviceWorkMode.accept($0 as? UInt8) }
        registerNotificationCallback(Command.NOTIFICATION_IN_EAR_STATUS, callableType: BoolPayloadHandler.self)           { self.deviceInEarStatus.accept($0 as? Bool) }
        registerNotificationCallback(Command.NOTIFICATION_LANGUAGE_SETTING, callableType: UInt8PayloadHandler.self)       { self.deviceLanguageSetting.accept($0 as? UInt8) }
        registerNotificationCallback(Command.NOTIFICATION_ANC_MODE, callableType: UInt8PayloadHandler.self)               { self.deviceAncMode.accept($0 as? UInt8) }
        registerNotificationCallback(Command.NOTIFICATION_TWS_CONNECTED, callableType: BoolPayloadHandler.self)           { self.deviceTwsConnected.accept($0 as? Bool) }
        registerNotificationCallback(Command.NOTIFICATION_LED_SWITCH, callableType: BoolPayloadHandler.self)              { self.deviceLedSwitch.accept($0 as? Bool) }
        registerNotificationCallback(Command.NOTIFICATION_ANC_GAIN, callableType: UInt8ToIntPayloadHandler.self)          { self.deviceAncGain.accept($0 as? Int) }
        registerNotificationCallback(Command.NOTIFICATION_TRANSPARENCY_GAIN, callableType: UInt8ToIntPayloadHandler.self) { self.deviceTransparencyGain.accept($0 as? Int) }
        registerNotificationCallback(Command.NOTIFICATION_MAIN_SIDE, callableType: BoolPayloadHandler.self)               { self.deviceLeftIsMainSide.accept($0 as? Bool) }
        registerNotificationCallback(Command.NOTIFICATION_SOUND_EFFECT_3D, callableType: BoolPayloadHandler.self)         { self.deviceSoundEffect3d.accept($0 as? Bool) }
    }
    
    /// 注册获取设备信息的处理类和回调
    /// callableType可以替换成自己想要的处理方法和结果
    private func registerDefaultDeviceInfoCallables() {
        
        registerDeviceInfoCallback(Command.INFO_DEVICE_POWER, callableType: PowerPayloadHandler.self)                   { self.devicePower.accept($0 as? DevicePower) }
        registerDeviceInfoCallback(Command.INFO_FIRMWARE_VERSION, callableType: UInt32PayloadHandler.self)              { self.deviceFirmwareVersion.accept($0 as? UInt32) }
        registerDeviceInfoCallback(Command.INFO_BLUETOOTH_NAME, callableType: StringPayloadHandler.self)                { self.deviceName.accept($0 as? String) }
        registerDeviceInfoCallback(Command.INFO_EQ_SETTING, callableType: RemoteEqSettingPayloadHandler.self)           { self.deviceEqSetting.accept($0 as? RemoteEqSetting) }
        registerDeviceInfoCallback(Command.INFO_KEY_SETTINGS, callableType: KeyPayloadHandler.self)                     { self.deviceKeySettings.accept($0 as? [KeyType: KeyFunction]) }
        registerDeviceInfoCallback(Command.INFO_DEVICE_VOLUME, callableType: UInt8PayloadHandler.self)                  { self.deviceVolume.accept($0 as? UInt8) }
        registerDeviceInfoCallback(Command.INFO_PLAY_STATE, callableType: BoolPayloadHandler.self)                      { self.devicePlayState.accept($0 as? Bool) }
        registerDeviceInfoCallback(Command.INFO_WORK_MODE, callableType: UInt8PayloadHandler.self)                      { self.deviceWorkMode.accept($0 as? UInt8) }
        registerDeviceInfoCallback(Command.INFO_IN_EAR_STATUS, callableType: BoolPayloadHandler.self)                   { self.deviceInEarStatus.accept($0 as? Bool) }
        registerDeviceInfoCallback(Command.INFO_LANGUAGE_SETTING, callableType: UInt8PayloadHandler.self)               { self.deviceLanguageSetting.accept($0 as? UInt8) }
        registerDeviceInfoCallback(Command.INFO_AUTO_ANSWER, callableType: BoolPayloadHandler.self)                     { self.deviceAutoAnswer.accept($0 as? Bool) }
        registerDeviceInfoCallback(Command.INFO_ANC_MODE, callableType: UInt8PayloadHandler.self)                       { self.deviceAncMode.accept($0 as? UInt8) }
        registerDeviceInfoCallback(Command.INFO_IS_TWS, callableType: BoolPayloadHandler.self)                          { self.deviceIsTws.accept($0 as? Bool) }
        registerDeviceInfoCallback(Command.INFO_TWS_CONNECTED, callableType: BoolPayloadHandler.self)                   { self.deviceTwsConnected.accept($0 as? Bool) }
        registerDeviceInfoCallback(Command.INFO_LED_SWITCH, callableType: BoolPayloadHandler.self)                      { self.deviceLedSwitch.accept($0 as? Bool) }
        registerDeviceInfoCallback(Command.INFO_FW_CHECKSUM, callableType: FirmwareChecksumPayloadHandler.self)         { self.deviceFwChecksum.accept($0 as? Data) }
        registerDeviceInfoCallback(Command.INFO_ANC_GAIN, callableType: UInt8ToIntPayloadHandler.self)                  { self.deviceAncGain.accept($0 as? Int) }
        registerDeviceInfoCallback(Command.INFO_TRANSPARENCY_GAIN, callableType: UInt8ToIntPayloadHandler.self)         { self.deviceTransparencyGain.accept($0 as? Int) }
        registerDeviceInfoCallback(Command.INFO_ANC_GAIN_NUM, callableType: UInt8ToIntPayloadHandler.self)              { self.deviceAncGainNum.accept($0 as? Int) }
        registerDeviceInfoCallback(Command.INFO_TRANSPARENCY_GAIN_NUM, callableType: UInt8ToIntPayloadHandler.self)     { self.deviceTransparencyGainNum.accept($0 as? Int) }
        registerDeviceInfoCallback(Command.INFO_ALL_EQ_SETTINGS, callableType: RemoteEqSettingsPayloadHandler.self)     { self.deviceRemoteEqSettings.accept($0 as? [RemoteEqSetting]) }
        registerDeviceInfoCallback(Command.INFO_MAIN_SIDE, callableType: BoolPayloadHandler.self)                       { self.deviceLeftIsMainSide.accept($0 as? Bool) }
        registerDeviceInfoCallback(Command.INFO_PRODUCT_COLOR, callableType: UInt8PayloadHandler.self)                  { self.deviceProductColor.accept($0 as? UInt8) }
        registerDeviceInfoCallback(Command.INFO_SOUND_EFFECT_3D, callableType: BoolPayloadHandler.self)                 { self.deviceSoundEffect3d.accept($0 as? Bool) }
        registerDeviceInfoCallback(Command.INFO_DEVICE_CAPABILITIES, callableType: DeviceCapacitiesPayloadHandler.self) { self.deviceCapacities.accept($0 as? DeviceCapacities) }
    }
    
    public func registerRequestToDeviceInfoDicts() {
        registerRequestToDeviceInfoDict(command: Command.COMMAND_EQ, deviceInfo: Command.INFO_EQ_SETTING)
        registerRequestToDeviceInfoDict(command: Command.COMMAND_WORK_MODE, deviceInfo: Command.INFO_WORK_MODE)
        registerRequestToDeviceInfoDict(command: Command.COMMAND_IN_EAR_DETECT, deviceInfo: Command.INFO_IN_EAR_STATUS)
        registerRequestToDeviceInfoDict(command: Command.COMMAND_LANGUAGE, deviceInfo: Command.INFO_LANGUAGE_SETTING)
        registerRequestToDeviceInfoDict(command: Command.COMMAND_AUTO_ANSWER, deviceInfo: Command.INFO_AUTO_ANSWER)
        registerRequestToDeviceInfoDict(command: Command.COMMAND_ANC_MODE, deviceInfo: Command.INFO_ANC_MODE)
        registerRequestToDeviceInfoDict(command: Command.COMMAND_BLUETOOTH_NAME, deviceInfo: Command.INFO_BLUETOOTH_NAME)
        registerRequestToDeviceInfoDict(command: Command.COMMAND_LED_MODE, deviceInfo: Command.INFO_LED_SWITCH)
        registerRequestToDeviceInfoDict(command: Command.COMMAND_ANC_GAIN, deviceInfo: Command.INFO_ANC_GAIN)
        registerRequestToDeviceInfoDict(command: Command.COMMAND_TRANSPARENCY_GAIN, deviceInfo: Command.INFO_TRANSPARENCY_GAIN)
        registerRequestToDeviceInfoDict(command: Command.COMMAND_SOUND_EFFECT_3D, deviceInfo: Command.INFO_SOUND_EFFECT_3D)
        
        registerRequestToDeviceInfoDict(command: Command.COMMAND_MUSIC_CONTROL, deviceInfo: Command.INFO_DEVICE_VOLUME)
        registerRequestToDeviceInfoDict(command: Command.COMMAND_KEY, deviceInfo: Command.INFO_KEY_SETTINGS)
    }
    
    public func resetDeviceStatus() {
        devicePower.accept(nil)
        deviceFirmwareVersion.accept(nil)
        deviceName.accept(nil)
        deviceEqSetting.accept(nil)
        deviceKeySettings.accept(nil)
        deviceVolume.accept(nil)
        devicePlayState.accept(nil)
        deviceWorkMode.accept(nil)
        deviceInEarStatus.accept(nil)
        deviceLanguageSetting.accept(nil)
        deviceAutoAnswer.accept(nil)
        deviceAncMode.accept(nil)
        deviceIsTws.accept(nil)
        deviceTwsConnected.accept(nil)
        deviceLedSwitch.accept(nil)
        deviceFwChecksum.accept(nil)
        deviceAncGain.accept(nil)
        deviceTransparencyGain.accept(nil)
        deviceAncGainNum.accept(nil)
        deviceTransparencyGainNum.accept(nil)
        deviceRemoteEqSettings.accept(nil)
        deviceLeftIsMainSide.accept(nil)
        deviceProductColor.accept(nil)
        deviceSoundEffect3d.accept(nil)
        deviceCapacities.accept(nil)
    }
    
}
