//
//  AutoAnswerRequest.swift
//  DeviceManager
//
//  Created by Bluetrum.
//  

import Foundation

public final class AutoAnswerRequest: Request {
    
    public private(set) var enable: Bool
    
    public init(_ enable: Bool) {
        self.enable = enable
        super.init(Command.COMMAND_AUTO_ANSWER)
    }
    
    public override func getPayload() -> Data {
        let value: UInt8 = enable ? 0x01 : 0x00
        return Data([value])
    }
    
}
