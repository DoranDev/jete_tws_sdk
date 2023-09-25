//
//  ResponsePayloadHandler.swift
//  DeviceManager
//
//  Created by Bluetrum on 2022/3/7.
//

import Foundation

public final class ResponsePayloadHandler: AbstractPayloadHandler {
    
    public override func callAsFunction() -> AnyObject? {
        
        if payload.count == 1 {
            return (payload[0] == 0x00) as AnyObject
        }
        return nil
    }
    
}
