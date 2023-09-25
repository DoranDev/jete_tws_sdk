//
//  UInt8ToIntPayloadHandler.swift
//  DeviceManager
//
//  Created by Bluetrum on 2022/3/7.
//

import Foundation

public final class UInt8ToIntPayloadHandler: AbstractPayloadHandler {
    
    public override func callAsFunction() -> AnyObject? {
        if payload.count == 1 {
            return Int(payload[0]) as AnyObject
        }
        return nil
    }
    
}
