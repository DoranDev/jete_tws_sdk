//
//  InEarDetectRequest.swift
//  DeviceManager
//
//  Created by Bluetrum.
//  

import Foundation

public final class InEarDetectRequest: Request {
    
    public private(set) var enable: Bool
    
    public init(_ enable: Bool) {
        self.enable = enable
        super.init(Command.COMMAND_IN_EAR_DETECT)
    }
    
    public override func getPayload() -> Data {
        let value: UInt8 = enable ? 0x01 : 0x00
        return Data([value])
    }
    
}
