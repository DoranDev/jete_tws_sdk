import 'dart:convert';

import 'package:flutter/services.dart';

class JeteTwsSdk {
  final methodChannel = const MethodChannel('jete_tws_sdk');

  Future bondDevice({required device}) =>
      methodChannel.invokeMethod('bondDevice', {'bmac': jsonEncode(device)});
  Future disconnect() => methodChannel.invokeMethod('disconnect');
  Future startScan() => methodChannel.invokeMethod('startScan');
  Future stopScan() => methodChannel.invokeMethod('stopScan');
  Future devicePower() => methodChannel.invokeMethod('devicePower');

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

  final EventChannel _devicePowerChannel = const EventChannel('devicePower');
  Stream get devicePowerStream => _devicePowerChannel
      .receiveBroadcastStream(_devicePowerChannel.name)
      .cast();

  final EventChannel _scanningStateChannel =
      const EventChannel('scanningState');
  Stream get scanningStateStream => _scanningStateChannel
      .receiveBroadcastStream(_scanningStateChannel.name)
      .cast();

  final EventChannel _deviceFirmwareVersionChannel =
      const EventChannel('deviceFirmwareVersion');
  Stream get deviceFirmwareVersionStream => _deviceFirmwareVersionChannel
      .receiveBroadcastStream(_deviceFirmwareVersionChannel.name)
      .cast();
}
