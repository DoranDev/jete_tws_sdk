import Flutter
import UIKit
import RxSwift

public class JeteTwsSdkPlugin: NSObject, FlutterPlugin, FlutterStreamHandler {
    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        switch arguments as? String {
        case JeteTwsSdkPlugin.eventChannelNameScannerResult:
            scannerResultSink = events
            break;
        case JeteTwsSdkPlugin.eventChannelNameScannerState:
            scannerStateSink = events
            break;
        case JeteTwsSdkPlugin.eventChannelNameDeviceConnectionState:
            deviceConnectionStateSink = events
            break;
        case JeteTwsSdkPlugin.eventChannelNamePopupDevice:
            popupDeviceSink = events
            break;
        case JeteTwsSdkPlugin.eventChannelNameActiveDevice:
            activeDeviceSink = events
            break;
        case JeteTwsSdkPlugin.eventChannelNameDeviceInfo:
            deviceInfoSink = events
            break;
        case JeteTwsSdkPlugin.eventChannelNameScanningState:
            scanningStateSink = events
            break;
        default:
            break
        }
        return nil
    }

    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        return nil
    }
    
    private var channel: FlutterMethodChannel?
    // private var mContext: FlutterPluginRegistrar?
    // private var mActivity: UIViewController?
    // private var mDefaultDeviceCommManager: DefaultDeviceCommManager?
    // private var mDeviceManagerApi: DeviceManagerApi?
    private var mDeviceRepository: DeviceRepository = DeviceRepository.shared
    
    static let eventChannelNameScannerResult = "scannerResult";
    static let eventChannelNameScannerState = "scannerState";
    static let eventChannelNameDeviceConnectionState = "deviceConnectionState";
    static let eventChannelNamePopupDevice = "popupDevice";
    static let eventChannelNameActiveDevice = "activeDevice";
    static let eventChannelNameDeviceInfo = "deviceInfo";
    static let eventChannelNameScanningState = "scanningState";
    
    private var scannerResultSink: FlutterEventSink?
    private var scannerStateSink: FlutterEventSink?
    private var deviceConnectionStateSink: FlutterEventSink?
    private var popupDeviceSink: FlutterEventSink?
    private var activeDeviceSink: FlutterEventSink?
    private var deviceInfoSink: FlutterEventSink?
    private var scanningStateSink: FlutterEventSink?
    
    private var mDevices = [Dictionary<String, Any>]()
    private var mABDevices = [ABDevice]()
    private let disposeBag = DisposeBag()
    
    
    private func deviceMap(_ value: ABDevice) -> [String: Any] {
        let device = value as! ABEarbuds
        var item: [String: Any] = [:]
        item["deviceName"] = device.name
        item["deviceMacAddress"] = device.btAddress
        item["deviceBleAddress"] = device.peripheral.identifier.uuidString
        item["rssi"] = value.rssi
        return item
    }


    private func initDevice() {
        mDeviceRepository.latestDiscoveredDevice.subscribeOnNext { [weak self] device in
            if let value = device {
                let item = self?.deviceMap(value)

                if let existingIndex = self?.mDevices.firstIndex(where: { ($0 as [String: Any])["deviceMacAddress"] as? String == item?["deviceMacAddress"] as? String }) {
                    self?.mDevices[existingIndex] = item!
                } else {
                    self?.mABDevices.append(value)
                    self?.mDevices.append(item ?? [:])
                }


                self?.mDevices.sort { (dict1, dict2) -> Bool in
                    let rssi1 = (dict1 as [String: Any])["rssi"] as? Int ?? -100
                    let rssi2 = (dict2 as [String: Any])["rssi"] as? Int ?? -100
                    return rssi1 > rssi2
                }
            }
            self?.scannerResultSink?(self?.mDevices ?? [])
        }.disposed(by: disposeBag)


        mDeviceRepository.activeDevice.subscribeOnNext { [weak self] device in
            if(device != nil){
                let item = self?.deviceMap(device!)
                self?.activeDeviceSink?(item)
            }
        }.disposed(by: disposeBag)
    }



  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "jete_tws_sdk", binaryMessenger: registrar.messenger())
    let instance = JeteTwsSdkPlugin(channel)
    registrar.addMethodCallDelegate(instance, channel: channel)
      let scannerResultChannel = FlutterEventChannel(name: eventChannelNameScannerResult, binaryMessenger:registrar.messenger())
      scannerResultChannel.setStreamHandler(instance)
      let scannerStateChannel = FlutterEventChannel(name: eventChannelNameScannerState, binaryMessenger:registrar.messenger())
      scannerStateChannel.setStreamHandler(instance)
      let deviceConnectionStateChannel = FlutterEventChannel(name: eventChannelNameDeviceConnectionState, binaryMessenger:registrar.messenger())
      deviceConnectionStateChannel.setStreamHandler(instance)
      let popupDeviceChannel = FlutterEventChannel(name: eventChannelNamePopupDevice, binaryMessenger:registrar.messenger())
      popupDeviceChannel.setStreamHandler(instance)
      let activeDeviceChannel = FlutterEventChannel(name: eventChannelNameActiveDevice, binaryMessenger:registrar.messenger())
      activeDeviceChannel.setStreamHandler(instance)
      let deviceInfoChannel = FlutterEventChannel(name: eventChannelNameDeviceInfo, binaryMessenger:registrar.messenger())
      deviceInfoChannel.setStreamHandler(instance)
      let scanningStateChannel = FlutterEventChannel(name: eventChannelNameScanningState, binaryMessenger:registrar.messenger())
      scanningStateChannel.setStreamHandler(instance)
  }

    init (_ channel: FlutterMethodChannel) {
        super.init()
        initDevice()
   }


  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      switch call.method {
          case "startScan":
              startScan()
              result(nil)

          case "stopScan":
              stopScan()
              result(nil)

          case "bondDevice":
                if let args = call.arguments as? [String: Any] {
                  bondDevice(device: args["bmac"] as! String)
                  result(nil)
              } else {
                  result(FlutterError(code: "InvalidArgument", message: "Invalid argument for 'bmac'", details: nil))
              }

          case "disconnect":
              disconnect()
              result(nil)

          case "deviceInfo":
              deviceInfo()
              result(nil)

          case "headsetIsConnected":
              if let args = call.arguments as? [String: Any] {
                    result(deviceIsConnected(device: args["bmac"] as! String))
                } else {
                    result(false)
                }
          case "sendRequest":
              if let args = call.arguments as? [String: Any],
                 let strRequest = args["strRequest"] as? String {
                      let gain = args["gain"] as? UInt8
                      let mode = args["mode"] as? UInt8
                      let enable = args["enable"] as? Bool
                      let bluetoothName = args["bluetoothName"] as? String
                      let setting = args["setting"] as? UInt8
                      let eqmode = args["eqmode"] as? UInt8
                      let language = args["language"] as? UInt8
                      let eqGainList = args["eqgain"] as? [Int]
                      let isCustom = args["isCustom"] as? Bool
                      let keyType = args["keyType"] as? UInt8
                      let keyFunction = args["keyFunction"] as? UInt8
                      let controlType = args["controlType"] as? UInt8

                  var request: Request?
                  switch strRequest {
                      case "AncGainRequest":
                          request = gain.map { AncGainRequest($0) }
                      case "AncModeRequest":
                      request = mode.map { AncModeRequest(AncMode(rawValue: $0) ?? AncMode.off) }
                      case "AutoAnswerRequest":
                          request = enable.map { AutoAnswerRequest($0) }
                      case "AutoShutdownRequest":
                      request = setting.map { AutoShutdownRequest(AutoShutdownSetting.time($0)) }
                      case "BluetoothNameRequest":
                          request = BluetoothNameRequest(bluetoothName ?? "")
                      case "ClearPairRecordRequest":
                          request = ClearPairRecordRequest()
                      case "EqRequest":
                      if let eqGainsByteArray = eqGainList?.compactMap({ $0 }) {
                          let gainsIntArray = eqGainsByteArray.withUnsafeBytes { Array($0.bindMemory(to: Int8.self)) }
                          request = eqmode.map {
                              isCustom == true ?
                              EqRequest.CustomEqRequest(eqMode: $0, gains: gainsIntArray) :
                              EqRequest.PresetEqRequest(eqMode: $0, gains: gainsIntArray)
                          }
                      }
                      case "FactoryResetRequest":
                          request = FactoryResetRequest()
                      case "FindDeviceRequest":
                          request = enable.map { FindDeviceRequest($0) }
                      case "InEarDetectRequest":
                          request = enable.map { InEarDetectRequest($0) }
                      case "KeyRequest":
                          request = keyType.flatMap { keyTypeValue in
                              keyFunction.map { keyFunctionValue in
                                  KeyRequest(keyType: KeyType(rawValue: keyTypeValue) ?? KeyType.rightTripleTap, keyFunction: KeyFunction(rawValue: keyFunctionValue) ?? KeyFunction.none)
                              }
                          }
                      case "LanguageRequest":
                      request = language.map { LanguageRequest(LanguageSetting(rawValue: $0) ?? LanguageSetting.english) }
                      case "LedSwitchRequest":
                          request = enable.map { LedSwitchRequest($0) }
                      case "MusicControlRequest":
                      request = controlType.map { _ in MusicControlRequest(MusicControlType.next)}
                      case "SoundEffect3dRequest":
                          request = enable.map { SoundEffect3dRequest($0) }
                      case "TransparencyGainRequest":
                          request = gain.map { TransparencyGainRequest($0) }
                      case "WorkModeRequest":
                      request = mode.map { WorkModeRequest(WorkMode(rawValue: $0) ?? WorkMode.normal) }
                      default:
                          request = nil
                  }

                  if let request = request {
                      mDeviceRepository.deviceCommManager.sendRequest(request)
                      print("sendRequest", request)
                  }
              } else {
                  result(FlutterError(code: "InvalidArgument", message: "Invalid argument for 'sendRequest'", details: nil))
              }

          default:
              result(FlutterMethodNotImplemented)
          }
  }

    private func startScan() {
        print("startScan")
        mDevices.removeAll()
        mDeviceRepository.startScanning()
    }

    private func stopScan() {
        print("stopScan")
        mDeviceRepository.stopScanning()
    }

    private func disconnect() {
        print("disconnect")
        mDeviceRepository.disconnect()
    }
    
    
    private func deviceIsConnected(device: String) -> Bool{
        if let dictionary = try? JSONSerialization.jsonObject(with: device.data(using: .utf8)!, options: []) as? [String: Any] {
            let abDevice = deviceFromFlutter(device: dictionary, devices: mABDevices)
            if let abDevice = abDevice {
                return abDevice.isConnected
            }
        }
        return false
    }


    private func bondDevice(device: String) {
        if let dictionary = try? JSONSerialization.jsonObject(with: device.data(using: .utf8)!, options: []) as? [String: Any] {
            let abDevice = deviceFromFlutter(device: dictionary, devices: mABDevices)
            if let abDevice = abDevice {
                print("connect", device)
                mDeviceRepository.connect(abDevice)
            }
        }
    }

    private func deviceFromFlutter(device: [String: Any?], devices: [ABDevice]) -> ABDevice? {
        if devices.isEmpty {
            print("deviceFromFlutter: devices list is empty")
            return nil
        }

        for abDevice in devices {
            print("deviceFromFlutter: \(abDevice.peripheral.identifier)")
            if abDevice.peripheral.identifier.uuidString == device["deviceBleAddress"] as? String {
                return abDevice
            }
        }

        return nil
    }

    private func deviceInfo() {
        print("sendRequest", "deviceInfo")
        mDeviceRepository.deviceCommManager.sendRequest(DeviceInfoRequest.defaultInfoRequest)

        // Sleep for 500 milliseconds (0.5 seconds)
        Thread.sleep(forTimeInterval: 0.5)

        getLiveData()
    }

    private func getLiveData() {
        var devicePower = [String: Any]()
        let mdevicePower = mDeviceRepository.devicePower.value
        devicePower["leftSidePower"] = [
            "isCharging": mdevicePower?.leftSidePower?.isCharging ?? false,
            "powerLevel": mdevicePower?.leftSidePower?.powerLevel ?? 0
        ] as [String : Any]

        devicePower["rightSidePower"] = [
            "isCharging": mdevicePower?.rightSidePower?.isCharging ?? false,
            "powerLevel": mdevicePower?.rightSidePower?.powerLevel ?? 0
        ] as [String : Any]

        devicePower["casePower"] = [
            "isCharging": mdevicePower?.casePower?.isCharging ?? false,
            "powerLevel": mdevicePower?.casePower?.powerLevel ?? 0
        ] as [String : Any]

        var deviceEqSetting = [String: Any]()
        let mdeviceEqSetting = mDeviceRepository.deviceEqSetting.value
        deviceEqSetting["mode"] = mdeviceEqSetting?.mode
        deviceEqSetting["gains"] = mdeviceEqSetting?.gains

        var deviceKeySettings = [[String: Any]()]
        let mdeviceKeySettings = mDeviceRepository.deviceKeySettings.value
        mdeviceKeySettings?.forEach({ (key: KeyType, value: KeyFunction) in
            var deviceKeySetting = [String: Any]()
            deviceKeySetting[String(key.rawValue)] = value.rawValue
            deviceKeySettings.append(deviceKeySetting)
        })

        var deviceRemoteEqSettings = [[String: Any]()]
        let mdeviceRemoteEqSettings = mDeviceRepository.deviceRemoteEqSettings.value
        mdeviceRemoteEqSettings?.forEach({ RemoteEqSetting in
            var deviceEqSetting = [String: Any]()
            deviceEqSetting["mode"] = RemoteEqSetting.mode
            deviceEqSetting["gains"] = RemoteEqSetting.gains
            deviceRemoteEqSettings.append(deviceEqSetting)
        })

        
        var deviceInfo = [String: Any]()
        deviceInfo["devicePower"] = devicePower
        deviceInfo["deviceFirmwareVersion"] = mDeviceRepository.deviceFirmwareVersion.value
        deviceInfo["deviceName"] = mDeviceRepository.deviceName.value
        deviceInfo["deviceEqSetting"] = deviceEqSetting
        deviceInfo["deviceKeySettings"] = deviceKeySettings
        deviceInfo["deviceVolume"] = mDeviceRepository.deviceVolume.value
        deviceInfo["devicePlayState"] = mDeviceRepository.devicePlayState.value
        deviceInfo["deviceWorkMode"] = mDeviceRepository.deviceWorkMode.value
        deviceInfo["deviceInEarStatus"] = mDeviceRepository.deviceInEarStatus.value
        deviceInfo["deviceLanguageSetting"] = mDeviceRepository.deviceLanguageSetting.value
        deviceInfo["deviceAutoAnswer"] = mDeviceRepository.deviceAutoAnswer.value
        deviceInfo["deviceAncMode"] = mDeviceRepository.deviceAncMode.value
        deviceInfo["deviceIsTws"] = mDeviceRepository.deviceIsTws.value
        deviceInfo["deviceTwsConnected"] = mDeviceRepository.deviceTwsConnected.value
        deviceInfo["deviceLedSwitch"] = mDeviceRepository.deviceLedSwitch.value
        deviceInfo["deviceFwChecksum"] = mDeviceRepository.deviceFwChecksum.value
        deviceInfo["deviceAncGain"] = mDeviceRepository.deviceAncGain.value
        deviceInfo["deviceTransparencyGain"] = mDeviceRepository.deviceTransparencyGain.value
        deviceInfo["deviceAncGainNum"] = mDeviceRepository.deviceAncGainNum.value
        deviceInfo["deviceTransparencyGainNum"] = mDeviceRepository.deviceTransparencyGainNum.value
        deviceInfo["deviceRemoteEqSettings"] = deviceRemoteEqSettings
        deviceInfo["deviceLeftIsMainSide"] = mDeviceRepository.deviceLeftIsMainSide.value
        deviceInfo["deviceProductColor"] = mDeviceRepository.deviceProductColor.value
        deviceInfo["deviceSoundEffect3d"] = mDeviceRepository.deviceSoundEffect3d.value
        deviceInfo["deviceCapacities"] = mDeviceRepository.deviceCapacities.value?.rawValue
        deviceInfo["deviceMaxPacketSize"] = mDeviceRepository.deviceMaxPacketSize.value

        deviceInfoSink?(deviceInfo)
    }


//    func toJSON<T: Encodable>(_ value: T) -> String? {
//        let encoder = JSONEncoder()
//        do {
//            let jsonData = try encoder.encode(value)
//            let jsonString = String(data: jsonData, encoding: .utf8)
//            return jsonString
//        } catch {
//            print("Error encoding value to JSON: \(error)")
//            return nil
//        }
//    }
    
}
