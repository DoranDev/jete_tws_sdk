//
//  MtuPayloadHandler.swift
//  DeviceManager
//
//  Created by Bluetrum on 2022/3/7.
//

import Foundation

public final class MtuPayloadHandler: AbstractPayloadHandler {
    
    public override func callAsFunction() -> AnyObject? {
        if payload.count == 1 {
            // CBPeripheral.maximumWriteValueLength is Int
            return Int(payload[0]) as AnyObject
        }
        return nil
    }
    
}
