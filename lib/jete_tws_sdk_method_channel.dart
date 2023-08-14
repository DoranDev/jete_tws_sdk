import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'jete_tws_sdk_platform_interface.dart';

/// An implementation of [JeteTwsSdkPlatform] that uses method channels.
class MethodChannelJeteTwsSdk implements JeteTwsSdkPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('jete_tws_sdk');

  @override
  void bondDevice() {
    methodChannel.invokeMethod<String>('bondDevice');
  }

  @override
  void disconnect() {
    methodChannel.invokeMethod<String>('disconnect');
  }

  @override
  void startScan() {
    methodChannel.invokeMethod<String>('startScan');
  }

  @override
  void stopScan() {
    methodChannel.invokeMethod<String>('stopScan');
  }
}
