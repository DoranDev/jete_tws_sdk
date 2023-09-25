//
//  BoolResponseCallable.swift
//  DeviceManager
//
//  Created by Bluetrum.
//  

import Foundation

@available(*, deprecated, message: "Use class inheriting from 'PayloadHandler'.")
public final class BoolResponseCallable: AbstractResponseCallable {
    
    public override func callAsFunction() -> AnyObject? {
        
        if payload.count == 1 {
            return (payload[0] == 0x00) as AnyObject
        }
        return nil
    }
    
}
