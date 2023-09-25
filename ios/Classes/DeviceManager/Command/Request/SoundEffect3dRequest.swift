//
//  SoundEffect3dRequest.swift
//  DeviceManager
//
//  Created by Bluetrum on 2022/6/24.
//

import Foundation

public final class SoundEffect3dRequest: Request {
    
    public private(set) var on: Bool
    
    public init(_ on: Bool) {
        self.on = on
        super.init(Command.COMMAND_SOUND_EFFECT_3D)
    }
    
    public override func getPayload() -> Data {
        let value: UInt8 = on ? 0x01 : 0x00
        return Data([value])
    }
    
}
