//
//  PowerPayloadHandler.swift
//  DeviceManager
//
//  Created by Bluetrum on 2022/3/7.
//

import Foundation

public final class PowerPayloadHandler : AbstractPayloadHandler {
    
    public override func callAsFunction() -> AnyObject? {
        if payload.count != 0 {
            return DevicePower(powerData: payload) as AnyObject
        }
        return nil
    }
    
}
