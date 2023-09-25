//
//  NotificationByteCallable.swift
//  DeviceManager
//
//  Created by Bluetrum on 2022/3/3.
//

import Foundation

@available(*, deprecated, message: "Use class inheriting from 'PayloadHandler'.")
public final class NotificationByteCallable: AbstractNotificationCallable {
    
    public override func callAsFunction() -> AnyObject? {
        if payload.count == 1 {
            return payload[0] as AnyObject
        }
        return nil
    }
    
}
