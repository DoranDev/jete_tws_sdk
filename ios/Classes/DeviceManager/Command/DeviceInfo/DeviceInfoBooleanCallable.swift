//
//  DeviceInfoBooleanCallable.swift
//  DeviceManager
//
//  Created by Bluetrum.
//  

import Foundation

@available(*, deprecated, message: "Use class inheriting from 'PayloadHandler'.")
public final class DeviceInfoBooleanCallable: AbstractDeviceInfoCallable {
    
    public override func callAsFunction() -> AnyObject? {
        
        if payload.count == 1 {
            let value = payload[0]
            if value == 0x00 {
                return false as AnyObject
            } else if value == 0x01 {
                return true as AnyObject
            }
        }
        return nil
    }
    
}
