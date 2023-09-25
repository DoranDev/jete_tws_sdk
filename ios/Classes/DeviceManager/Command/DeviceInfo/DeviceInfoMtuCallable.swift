//
//  DeviceInfoMtuCallable.swift
//  DeviceManager
//
//  Created by Bluetrum.
//  

import Foundation

@available(*, deprecated, message: "Use class inheriting from 'PayloadHandler'.")
public final class DeviceInfoMtuCallable: AbstractDeviceInfoCallable {
    
    public override func callAsFunction() -> AnyObject? {
        if payload.count == 1 {
            // CBPeripheral.maximumWriteValueLength is Int
            return Int(payload[0]) as AnyObject
        }
        return nil
    }
    
}
