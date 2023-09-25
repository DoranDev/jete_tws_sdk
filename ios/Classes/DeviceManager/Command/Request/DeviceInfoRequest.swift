//
//  DeviceInfoRequest.swift
//  DeviceManager
//
//  Created by Bluetrum.
//  

import Foundation

public final class DeviceInfoRequest: Request {
    
    private var payload: Data = Data()
    
    public init() {
        super.init(Command.COMMAND_DEVICE_INFO)
    }
    
    public override init(_ info: UInt8) {
        super.init(Command.COMMAND_DEVICE_INFO)
        requireInfo(info)
    }
    
    @discardableResult
    public func requireInfo(_ info: UInt8) -> Self {
        payload.append(info)
        payload.append(0)
        return self
    }
    
    public func requireDevicePower() -> DeviceInfoRequest {
        requireInfo(Command.INFO_DEVICE_POWER)
    }
    
    public func requireFirmwareVersion() -> DeviceInfoRequest {
        requireInfo(Command.INFO_FIRMWARE_VERSION)
    }
    
    public func requireBluetoothName() -> DeviceInfoRequest {
        requireInfo(Command.INFO_BLUETOOTH_NAME)
    }
    
    public func requireEqSettings() -> DeviceInfoRequest {
        requireInfo(Command.INFO_EQ_SETTING)
    }
    
    public func requireKeySettings() -> DeviceInfoRequest {
        requireInfo(Command.INFO_KEY_SETTINGS)
    }
    
    public func requireDeviceVolume() -> DeviceInfoRequest {
        requireInfo(Command.INFO_DEVICE_VOLUME)
    }
    
    public func requirePlayState() -> DeviceInfoRequest {
        requireInfo(Command.INFO_PLAY_STATE)
    }
    
    public func requireWorkMode() -> DeviceInfoRequest {
        requireInfo(Command.INFO_WORK_MODE)
    }
    
    public func requireInEarStatus() -> DeviceInfoRequest {
        requireInfo(Command.INFO_IN_EAR_STATUS)
    }
    
    public func requireLanguageSetting() -> DeviceInfoRequest {
        requireInfo(Command.INFO_LANGUAGE_SETTING)
    }
    
    public func requireAutoAnswer() -> DeviceInfoRequest {
        requireInfo(Command.INFO_AUTO_ANSWER)
    }
    
    public func requireAncMode() -> DeviceInfoRequest {
        requireInfo(Command.INFO_ANC_MODE)
    }
    
    public func requireIsTws() -> DeviceInfoRequest {
        requireInfo(Command.INFO_IS_TWS)
    }
    
    public func requireTwsConnected() -> DeviceInfoRequest {
        requireInfo(Command.INFO_TWS_CONNECTED)
    }
    
    public func requireLedSwitch() -> DeviceInfoRequest {
        requireInfo(Command.INFO_LED_SWITCH)
    }
    
    public func requireFwChecksum() -> DeviceInfoRequest {
        requireInfo(Command.INFO_FW_CHECKSUM)
    }
    
    public func requireAncGain() -> DeviceInfoRequest {
        requireInfo(Command.INFO_ANC_GAIN)
    }
    
    public func requireTransparencyGain() -> DeviceInfoRequest {
        requireInfo(Command.INFO_TRANSPARENCY_GAIN)
    }
    
    public func requireAncGainNum() -> DeviceInfoRequest {
        requireInfo(Command.INFO_ANC_GAIN_NUM)
    }
    
    public func requireTransparencyGainNum() -> DeviceInfoRequest {
        requireInfo(Command.INFO_TRANSPARENCY_GAIN_NUM)
    }
    
    public func requireAllEqSettings() -> DeviceInfoRequest {
        requireInfo(Command.INFO_ALL_EQ_SETTINGS)
    }
    
    public func requireMainSide() -> DeviceInfoRequest {
        requireInfo(Command.INFO_MAIN_SIDE)
    }
    
    public func requireProductColor() -> DeviceInfoRequest {
        requireInfo(Command.INFO_PRODUCT_COLOR)
    }
    
    public func requireSoundEffect3d() -> DeviceInfoRequest {
        requireInfo(Command.INFO_SOUND_EFFECT_3D)
    }
    
    public func requireDeviceCapabilities() -> DeviceInfoRequest {
        requireInfo(Command.INFO_DEVICE_CAPABILITIES)
    }
    
    public func requireMaxPacketSize() -> DeviceInfoRequest {
        requireInfo(Command.INFO_MAX_PACKET_SIZE)
    }
    
    public static var defaultInfoRequest: DeviceInfoRequest {
        return DeviceInfoRequest()
            .requireDevicePower()
            .requireFirmwareVersion()
            .requireBluetoothName()
            .requireEqSettings()
            .requireKeySettings()
            .requireDeviceVolume()
            .requirePlayState()
            .requireWorkMode()
            .requireInEarStatus()
            .requireLanguageSetting()
            .requireAutoAnswer()
            .requireAncMode()
            .requireIsTws()
            .requireTwsConnected()
            .requireLedSwitch()
            .requireFwChecksum()
            .requireAncGain()
            .requireTransparencyGain()
            .requireAncGainNum()
            .requireTransparencyGainNum()
            .requireAllEqSettings()
            .requireMainSide()
            .requireProductColor()
            .requireSoundEffect3d()
            .requireDeviceCapabilities()
    }
    
    public override func getPayload() -> Data { payload }
    
}
