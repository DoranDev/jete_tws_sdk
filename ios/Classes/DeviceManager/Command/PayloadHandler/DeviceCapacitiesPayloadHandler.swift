//
//  DeviceCapacitiesPayloadHandler.swift
//  DeviceManager
//
//  Created by Bluetrum on 2022/6/24.
//

import Foundation

public struct DeviceCapacities: OptionSet {
    public let rawValue: UInt16
    
    public init(rawValue: UInt16) {
        self.rawValue = rawValue
    }
    
    public static let supportTws            = DeviceCapacities(rawValue: 1 << 0)
    public static let supportSoundEffect3d  = DeviceCapacities(rawValue: 1 << 1)
    public static let supportMultipoint     = DeviceCapacities(rawValue: 1 << 2)
    public static let supportAnc            = DeviceCapacities(rawValue: 1 << 3)
}

public final class DeviceCapacitiesPayloadHandler: AbstractPayloadHandler {
    
    public override func callAsFunction() -> AnyObject? {
        if payload.count == 2 {
            let value = UInt16(payload[1]) << 8 | UInt16(payload[0])
            let deviceCapacities = DeviceCapacities(rawValue: value)
            return deviceCapacities as AnyObject
        }
        return nil
    }
    
}
