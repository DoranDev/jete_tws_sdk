//
//  DeviceInfoModel.swift
//  jete_tws_sdk
//


import Foundation

struct DeviceInfoModel {
    let devicePower: [String: Any]?
    let deviceFirmwareVersion: UInt32?
    let deviceName: String?
    let deviceEqSetting: [String: Any]?
    let deviceKeySettings: [[String: Any]]?
    let deviceVolume: UInt8?
    let devicePlayState: Bool?
    let deviceWorkMode: UInt8?
    let deviceInEarStatus: Bool?
    let deviceLanguageSetting: UInt8?
    let deviceAutoAnswer: Bool?
    let deviceAncMode: UInt8?
    let deviceIsTws: Bool?
    let deviceTwsConnected: Bool?
    let deviceLedSwitch: Bool?
    let deviceFwChecksum: Data?
    let deviceAncGain: Int?
    let deviceTransparencyGain: Int?
    let deviceAncGainNum: Int?
    let deviceTransparencyGainNum: Int?
    let deviceRemoteEqSettings: [[String: Any]]?
    let deviceLeftIsMainSide: Bool?
    let deviceProductColor: UInt8?
    let deviceSoundEffect3d: Bool?
    let deviceCapacities: UInt16?
    let deviceMaxPacketSize: UInt16?
}


