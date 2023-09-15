//
//  OtaInfoResponseCallable.swift
//  FOTA
//
//  Created by Bluetrum.
//  

import Foundation


public final class OtaInfoResponseCallable: AbstractPayloadHandler {
    
    public override func callAsFunction() -> AnyObject? {
        return OtaInfo(payload)
    }
    
}
