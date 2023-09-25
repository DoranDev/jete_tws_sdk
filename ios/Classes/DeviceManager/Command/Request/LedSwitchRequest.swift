//
//  LedSwitchRequest.swift
//  DeviceManager
//
//  Created by Bluetrum.
//  

import Foundation

public final class LedSwitchRequest: Request {
    
    public private(set) var ledOn: Bool
    
    public init(_ ledOn: Bool) {
        self.ledOn = ledOn
        super.init(Command.COMMAND_LED_MODE)
    }
    
    public override func getPayload() -> Data {
        let value: UInt8 = ledOn ? 0x01 : 0x00
        return Data([value])
    }
    
}
