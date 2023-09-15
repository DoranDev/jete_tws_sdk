//
//  OtaError.swift
//  AB OTA Demo
//
//  Created by Bluetrum on 2020/12/25.
//

import Foundation

/// 升级过程中的错误，`OtaManagerDelegate`代理方法`onError`中使用
public enum OTAError: Error {
    
    /// 蓝牙未打开
    case centralManagerNotPoweredOn

    /// 设备不支持蓝讯FOTA升级
    case deviceNotSupported
    /// 设备已经关闭
    case deviceClosed
    /// 设备拒绝升级
    case refusedByDevice
    /// 没有FOTA数据可用
    case noDataAvailable
    
    /// 设备报告错误，含错误代码
    case deviceReport(code: UInt8)
    
    /// 等待设备回复超时
    case timeoutWaitingResponse
}

extension OTAError: LocalizedError {
    
    /// 错误描述
    public var errorDescription: String? {
        switch self {
        case .centralManagerNotPoweredOn: return NSLocalizedString("error_central_manager_not_powered_on", comment: "")
        case .deviceNotSupported: return NSLocalizedString("error_device_not_support", comment: "")
        case .deviceClosed: return NSLocalizedString("error_device_closed", comment: "")
        case .refusedByDevice: return NSLocalizedString("error_refused_by_device", comment: "")
        case .noDataAvailable: return NSLocalizedString("error_no_ota_data_available", comment: "")
        case .deviceReport(let code): return String(format: NSLocalizedString("error_device_report_error_code", comment: ""), code)
        case .timeoutWaitingResponse: return NSLocalizedString("error_wait_response_timeout", comment: "")
        }
    }
    
}
