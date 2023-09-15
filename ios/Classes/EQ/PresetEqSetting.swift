//
//  PresetEqSetting.swift
//  ABMate
//
//  Created by Bluetrum on 2022/2/25.
//

import Foundation

class PresetEqSetting: EqSetting {
    
    private static let eqGainsDefault: [Int8] = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ]
    private static let eqGainsPop: [Int8]     = [ 3, 1, 0, -2, -4, -4, -2, 0, 1, 2, ]
    private static let eqGainsRock: [Int8]    = [ -2, 0, 2, 4, -2, -2, 0, 0, 4, 4, ]
    private static let eqGainsJazz: [Int8]    = [ 0, 0, 0, 4, 4, 4, 0, 2, 3, 4, ]
    private static let eqGainsClassic: [Int8] = [ 0, 8, 8, 4, 0, 0, 0, 0, 2, 2, ]
    private static let eqGainsCountry: [Int8] = [ -2, 0, 0, 2, 2, 0, 0, 0, 4, 4, ]
    
    public static let eqSettingDefault = PresetEqSetting(name: NSLocalizedString("eq_setting_default", comment: ""), gains: eqGainsDefault)
    public static let eqSettingPop     = PresetEqSetting(name: NSLocalizedString("eq_setting_pop", comment: ""), gains: eqGainsPop)
    public static let eqSettingRock    = PresetEqSetting(name: NSLocalizedString("eq_setting_rock", comment: ""), gains: eqGainsRock)
    public static let eqSettingJazz    = PresetEqSetting(name: NSLocalizedString("eq_setting_jazz", comment: ""), gains: eqGainsJazz)
    public static let eqSettingClassic = PresetEqSetting(name: NSLocalizedString("eq_setting_classic", comment: ""), gains: eqGainsClassic)
    public static let eqSettingCountry = PresetEqSetting(name: NSLocalizedString("eq_setting_country", comment: ""), gains: eqGainsCountry)
    // Flag
    public static let eqSettingAdd     = PresetEqSetting(name: NSLocalizedString("eq_setting_add", comment: ""), gains: eqGainsDefault)
    
    private override init(name: String, gains: [Int8]) {
        super.init(name: name, gains: gains)
        isCustom = false
    }
    
    public static var allPresetEqSettings: [EqSetting] {
        return [
            eqSettingDefault,
            eqSettingPop,
            eqSettingRock,
            eqSettingJazz,
            eqSettingClassic,
            eqSettingCountry,
        ]
    }
    
}
