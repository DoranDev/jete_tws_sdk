//
//  OtaStateNotificationCallable.swift
//  FOTA
//
//  Created by Bluetrum.
//  

import Foundation
import DeviceManager

public final class OtaStateNotificationCallable: AbstractPayloadHandler {
    
    public override func callAsFunction() -> AnyObject? {
        if payload.count == 1 {
            return payload[0] as AnyObject
        }
        return nil
    }
    
}
