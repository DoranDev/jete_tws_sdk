//
//  DeviceInfoModel.swift
//  jete_tws_sdk
//
//  Created by Kakzaki.dev on 08/09/23.
//

import Foundation

struct DeviceInfoModel: Encodable {
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

    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)

        // Manually encode the dictionaries as strings
        try container.encode(encodeDictionary(devicePower), forKey: .devicePower)
        try container.encode(deviceFirmwareVersion, forKey: .deviceFirmwareVersion)
        try container.encode(deviceName, forKey: .deviceName)
        try container.encode(encodeDictionary(deviceEqSetting), forKey: .deviceEqSetting)
        try container.encode(encodeArrayOfDictionaries(deviceKeySettings), forKey: .deviceKeySettings)
        try container.encode(deviceVolume, forKey: .deviceVolume)
        try container.encode(devicePlayState, forKey: .devicePlayState)
        try container.encode(deviceWorkMode, forKey: .deviceWorkMode)
        try container.encode(deviceInEarStatus, forKey: .deviceInEarStatus)
        try container.encode(deviceLanguageSetting, forKey: .deviceLanguageSetting)
        try container.encode(deviceAutoAnswer, forKey: .deviceAutoAnswer)
        try container.encode(deviceAncMode, forKey: .deviceAncMode)
        try container.encode(deviceIsTws, forKey: .deviceIsTws)
        try container.encode(deviceTwsConnected, forKey: .deviceTwsConnected)
        try container.encode(deviceLedSwitch, forKey: .deviceLedSwitch)
        try container.encode(deviceFwChecksum, forKey: .deviceFwChecksum)
        try container.encode(deviceAncGain, forKey: .deviceAncGain)
        try container.encode(deviceTransparencyGain, forKey: .deviceTransparencyGain)
        try container.encode(deviceAncGainNum, forKey: .deviceAncGainNum)
        try container.encode(deviceTransparencyGainNum, forKey: .deviceTransparencyGainNum)
        try container.encode(encodeArrayOfDictionaries(deviceRemoteEqSettings), forKey: .deviceRemoteEqSettings)
        try container.encode(deviceLeftIsMainSide, forKey: .deviceLeftIsMainSide)
        try container.encode(deviceProductColor, forKey: .deviceProductColor)
        try container.encode(deviceSoundEffect3d, forKey: .deviceSoundEffect3d)
        try container.encode(deviceCapacities, forKey: .deviceCapacities)
        try container.encode(deviceMaxPacketSize, forKey: .deviceMaxPacketSize)
    }

    // Define the CodingKeys enum for your properties
    enum CodingKeys: String, CodingKey {
        case devicePower
        case deviceFirmwareVersion
        case deviceName
        case deviceEqSetting
        case deviceKeySettings
        case deviceVolume
        case devicePlayState
        case deviceWorkMode
        case deviceInEarStatus
        case deviceLanguageSetting
        case deviceAutoAnswer
        case deviceAncMode
        case deviceIsTws
        case deviceTwsConnected
        case deviceLedSwitch
        case deviceFwChecksum
        case deviceAncGain
        case deviceTransparencyGain
        case deviceAncGainNum
        case deviceTransparencyGainNum
        case deviceRemoteEqSettings
        case deviceLeftIsMainSide
        case deviceProductColor
        case deviceSoundEffect3d
        case deviceCapacities
        case deviceMaxPacketSize
    }

    private func encodeDictionary(_ dictionary: [String: Any]?) -> String? {
        if let dictionary = dictionary {
            do {
                let jsonData = try JSONSerialization.data(withJSONObject: dictionary, options: [])
                return String(data: jsonData, encoding: .utf8)
            } catch {
                return nil
            }
        }
        return nil
    }

    private func encodeArrayOfDictionaries(_ arrayOfDictionaries: [[String: Any]]?) -> [String]? {
        if let arrayOfDictionaries = arrayOfDictionaries {
            return arrayOfDictionaries.compactMap { encodeDictionary($0) }
        }
        return nil
    }
}


