//
//  AncModeRequest.swift
//  DeviceManager
//
//  Created by Bluetrum.
//  

import Foundation

public enum AncMode: UInt8 {
    case off            = 0x00
    case on             = 0x01
    case transparency   = 0x02
}

public final class AncModeRequest: Request {
    
    public private(set) var mode: AncMode
    
    public init(_ mode: AncMode) {
        self.mode = mode
        super.init(Command.COMMAND_ANC_MODE)
    }
    
    public override func getPayload() -> Data {
        return Data([mode.rawValue])
    }
    
}
