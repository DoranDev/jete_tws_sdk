import 'jete_tws_sdk_platform_interface.dart';

class JeteTwsSdk {
  void startScan() {
    JeteTwsSdkPlatform.instance.startScan();
  }

  void stopScan() {
    JeteTwsSdkPlatform.instance.stopScan();
  }

  void disconnect() {
    JeteTwsSdkPlatform.instance.disconnect();
  }

  void bondDevice() {
    JeteTwsSdkPlatform.instance.bondDevice();
  }
}
