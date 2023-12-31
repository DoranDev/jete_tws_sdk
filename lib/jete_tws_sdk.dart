import 'dart:convert';
import 'dart:developer';
import 'dart:io';
import 'package:flutter/services.dart';
import 'package:jete_tws_sdk/model/device_info_model.dart';
part 'command/tws_anc_mode.dart';
part 'command/tws_auto_shutdown.dart';
part 'command/tws_command.dart';
part 'command/tws_key.dart';
part 'command/tws_language.dart';
part 'command/tws_music_control.dart';
part 'command/tws_work_mode.dart';

class JeteTwsSdk {
  final methodChannel = const MethodChannel('jete_tws_sdk');

  void bondDevice({required device}) =>
      methodChannel.invokeMethod('bondDevice', {'bmac': jsonEncode(device)});

  Future headsetIsConnected({required device}) =>
      methodChannel.invokeMethod('headsetIsConnected', {
        'bmac': Platform.isIOS ? jsonEncode(device) : device['deviceMacAddress']
      });

  void disconnect() => methodChannel.invokeMethod('disconnect');

  void gotoBluetoothSettingIOS() =>
      methodChannel.invokeMethod('gotoBluetoothSettingIOS');

  void startScan() => methodChannel.invokeMethod('startScan');
  void stopScan() => methodChannel.invokeMethod('stopScan');

  void deviceInfo() => methodChannel.invokeMethod('deviceInfo');
  void ancGainRequest(int gain) => methodChannel.invokeMethod(
      'sendRequest', {'strRequest': 'AncGainRequest', 'gain': gain});
  void ancModeRequest(int mode) => methodChannel.invokeMethod(
      'sendRequest', {'strRequest': 'AncModeRequest', 'mode': mode});
  void autoAnswerRequest(bool enable) => methodChannel.invokeMethod(
      'sendRequest', {'strRequest': 'AutoAnswerRequest', 'enable': enable});
  void autoShutdownRequest(int setting) => methodChannel.invokeMethod(
      'sendRequest', {'strRequest': 'AutoShutdownRequest', 'setting': setting});
  void bluetoothNameRequest(String bluetoothName) => methodChannel.invokeMethod(
      'sendRequest',
      {'strRequest': 'BluetoothNameRequest', 'bluetoothName': bluetoothName});
  void clearPairRecordRequest() => methodChannel.invokeMethod('sendRequest', {
        'strRequest': 'ClearPairRecordRequest',
      });
  void eqRequest(int eqmode, List<int> eqgain, {bool isCustom = false}) =>
      methodChannel.invokeMethod('sendRequest', {
        'strRequest': 'EqRequest',
        'eqmode': eqmode,
        'eqgain': eqgain,
        'isCustom': isCustom
      });
  void factoryResetRequest() => methodChannel
      .invokeMethod('sendRequest', {'strRequest': 'FactoryResetRequest'});
  void findDeviceRequest(bool enable) => methodChannel.invokeMethod(
      'sendRequest', {'strRequest': 'FindDeviceRequest', 'enable': enable});
  void inEarDetectRequest(bool enable) => methodChannel.invokeMethod(
      'sendRequest', {'strRequest': 'InEarDetectRequest', 'enable': enable});
  void keyRequest(int keyType, int keyFunction) =>
      methodChannel.invokeMethod('sendRequest', {
        'strRequest': 'KeyRequest',
        'keyType': keyType,
        'keyFunction': keyFunction
      });
  void languageRequest(int language) => methodChannel.invokeMethod(
      'sendRequest', {'strRequest': 'LanguageRequest', 'language': language});
  void ledSwitchRequest(bool enable) => methodChannel.invokeMethod(
      'sendRequest', {'strRequest': 'LedSwitchRequest', 'enable': enable});
  void musicControlRequest(int controlType) => methodChannel.invokeMethod(
      'sendRequest',
      {'strRequest': 'MusicControlRequest', 'controlType': controlType});
  void soundEffect3dRequest(bool enable) => methodChannel.invokeMethod(
      'sendRequest', {'strRequest': 'SoundEffect3dRequest', 'enable': enable});
  void transparencyGainRequest(int gain) => methodChannel.invokeMethod(
      'sendRequest', {'strRequest': 'TransparencyGainRequest', 'gain': gain});
  void workModeRequest(int mode) => methodChannel.invokeMethod(
      'sendRequest', {'strRequest': 'WorkModeRequest', 'mode': mode});

  final EventChannel _scannerResultChannel =
      const EventChannel('scannerResult');
  Stream get scannerResultStream => _scannerResultChannel
      .receiveBroadcastStream(_scannerResultChannel.name)
      .cast();

  final EventChannel _scannerStateChannel = const EventChannel('scannerState');
  Stream get scannerStateStream => _scannerStateChannel
      .receiveBroadcastStream(_scannerStateChannel.name)
      .cast();

  final EventChannel _deviceConnectionStateChannel =
      const EventChannel('deviceConnectionState');
  Stream get deviceConnectionStateStream => _deviceConnectionStateChannel
      .receiveBroadcastStream(_deviceConnectionStateChannel.name)
      .cast();

  final EventChannel _popupDeviceChannel = const EventChannel('popupDevice');
  Stream get popupDeviceStream => _popupDeviceChannel
      .receiveBroadcastStream(_popupDeviceChannel.name)
      .cast();

  final EventChannel _activeDeviceChannel = const EventChannel('activeDevice');
  Stream get activeDeviceStream => _activeDeviceChannel
      .receiveBroadcastStream(_activeDeviceChannel.name)
      .cast();

  final EventChannel _deviceInfoChannel = const EventChannel('deviceInfo');
  Stream<DeviceInfo?> get deviceInfoStream => _deviceInfoChannel
          .receiveBroadcastStream(_deviceInfoChannel.name)
          .map<DeviceInfo?>((dynamic json) {
        if (Platform.isIOS) {
          Map<String, dynamic> map = Map<String, dynamic>.from(json ?? {});
          // Map<String, dynamic> devicePowerMap = Map<String, dynamic>.from(json["devicePower"]);
          // devicePowerMap.forEach((key, value) {
          //   final powerMap = Map<String, dynamic>.from(value);
          //   devicePowerMap["$key"] = powerMap;
          // });
          // map["devicePower"] = devicePowerMap;
          // map["deviceFirmwareVersion"] = json["deviceFirmwareVersion"];
          // map["deviceName"] = json["deviceName"];
          // map["deviceEqSetting"] = json["deviceEqSetting"];
          // map["deviceKeySettings"] = json["deviceKeySettings"];
          // map["deviceVolume"] = json["deviceVolume"];
          // map["devicePlayState"] = json["devicePlayState"];
          // map["deviceWorkMode"] = json["deviceWorkMode"];
          // map["deviceInEarStatus"] = json["deviceInEarStatus"];
          // map["deviceLanguageSetting"] = json["deviceLanguageSetting"];
          // map["deviceAutoAnswer"] = json["deviceAutoAnswer"];
          // map["deviceAncMode"] = json["deviceAncMode"];
          // map["deviceIsTws"] = json["deviceIsTws"];
          // map["deviceTwsConnected"] = json["deviceTwsConnected"];
          // map["deviceLedSwitch"] = json["deviceLedSwitch"];
          // map["deviceFwChecksum"] = json["deviceFwChecksum"];
          // map["deviceAncGain"] = json["deviceAncGain"];
          // map["deviceTransparencyGain"] = json["deviceTransparencyGain"];
          // map["deviceAncGainNum"] = json["deviceAncGainNum"];
          // map["deviceTransparencyGainNum"] = json["deviceTransparencyGainNum"];
          // map["deviceRemoteEqSettings"] = json["deviceRemoteEqSettings"];
          // map["deviceLeftIsMainSide"] = json["deviceLeftIsMainSide"];
          // map["deviceProductColor"] = json["deviceProductColor"];
          // map["deviceSoundEffect3d"] = json["deviceSoundEffect3d"];
          // map["deviceCapacities"] = json["deviceCapacities"];
          // map["deviceMaxPacketSize"] = json["deviceMaxPacketSize"];
          return DeviceInfo.fromJson(map);
        } else {
          try {
            Map<String, dynamic> decodedJson = jsonDecode(json);
            return DeviceInfo.fromJson(decodedJson);
          } catch (e) {
            log(e.toString());
            return null;
          }
        }
      });

  final EventChannel _scanningStateChannel =
      const EventChannel('scanningState');
  Stream get scanningStateStream => _scanningStateChannel
      .receiveBroadcastStream(_scanningStateChannel.name)
      .cast();
}
