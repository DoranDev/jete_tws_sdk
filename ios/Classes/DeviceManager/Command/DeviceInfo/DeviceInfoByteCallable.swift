//
//  DeviceInfoByteCallable.swift
//  DeviceManager
//
//  Created by Bluetrum.
//  

import Foundation

@available(*, deprecated, message: "Use class inheriting from 'PayloadHandler'.")
public final class DeviceInfoByteCallable: AbstractDeviceInfoCallable {
    
    public override func callAsFunction() -> AnyObject? {
        if payload.count == 1 {
            return payload[0] as AnyObject
        }
        return nil
    }
    
}
