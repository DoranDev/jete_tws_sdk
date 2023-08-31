import 'dart:convert';
import 'dart:developer';
import 'package:flutter/services.dart';
import 'package:jete_tws_sdk/model/device_info_model.dart';
part 'tws_anc_mode.dart';
part 'tws_auto_shutdown.dart';
part 'tws_command.dart';
part 'tws_key.dart';
part 'tws_language.dart';
part 'tws_music_control.dart';
part 'tws_work_mode.dart';

class JeteTwsSdk {
  final methodChannel = const MethodChannel('jete_tws_sdk');

  void bondDevice({required device}) =>
      methodChannel.invokeMethod('bondDevice', {'bmac': jsonEncode(device)});
  void disconnect() => methodChannel.invokeMethod('disconnect');
  void startScan() => methodChannel.invokeMethod('startScan');
  void stopScan() => methodChannel.invokeMethod('stopScan');

  void deviceInfo() => methodChannel.invokeMethod('deviceInfo');
  void ancGainRequest(int gain) =>
      methodChannel.invokeMethod('AncGainRequest', {'gain': gain});
  void ancModeRequest(int mode) =>
      methodChannel.invokeMethod('AncModeRequest', {'mode': mode});
  void autoAnswerRequest(bool enable) =>
      methodChannel.invokeMethod('AutoAnswerRequest', {'enable': enable});
  void autoShutdownRequest(int setting) =>
      methodChannel.invokeMethod('AutoShutdownRequest', {'setting': setting});
  void bluetoothNameRequest(String bluetoothName) => methodChannel
      .invokeMethod('BluetoothNameRequest', {'bluetoothName': bluetoothName});
  void clearPairRecordRequest() =>
      methodChannel.invokeMethod('ClearPairRecordRequest');
  void eqRequest(int eqmode, int eqgain, {bool isCustom = false}) =>
      methodChannel.invokeMethod('EqRequest',
          {'eqmode': eqmode, 'eqgain': eqgain, 'isCustom': isCustom});
  void factoryResetRequest() =>
      methodChannel.invokeMethod('FactoryResetRequest');
  void findDeviceRequest(bool enable) =>
      methodChannel.invokeMethod('FindDeviceRequest', {'enable': enable});
  void inEarDetectRequest(bool enable) =>
      methodChannel.invokeMethod('InEarDetectRequest', {'enable': enable});
  void keyRequest(int keyType, int keyFunction) => methodChannel.invokeMethod(
      'KeyRequest', {'keyType': keyType, 'keyFunction': keyFunction});
  void languageRequest(int language) =>
      methodChannel.invokeMethod('LanguageRequest', {'language': language});
  void ledSwitchRequest(bool enable) =>
      methodChannel.invokeMethod('LedSwitchRequest', {'enable': enable});
  void musicControlRequest(int controlType) => methodChannel
      .invokeMethod('MusicControlRequest', {'controlType': controlType});
  void soundEffect3dRequest(bool enable) =>
      methodChannel.invokeMethod('SoundEffect3dRequest', {'enable': enable});
  void transparencyGainRequest(int gain) =>
      methodChannel.invokeMethod('TransparencyGainRequest', {'gain': gain});
  void workModeRequest(int mode) =>
      methodChannel.invokeMethod('WorkModeRequest', {'mode': mode});

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
        try {
          Map<String, dynamic> decodedJson = jsonDecode(json);
          return DeviceInfo.fromJson(decodedJson);
        } catch (e) {
          log(e.toString());
          return null;
        }
      });

  final EventChannel _scanningStateChannel =
      const EventChannel('scanningState');
  Stream get scanningStateStream => _scanningStateChannel
      .receiveBroadcastStream(_scanningStateChannel.name)
      .cast();
}
