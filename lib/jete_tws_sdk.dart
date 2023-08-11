
import 'jete_tws_sdk_platform_interface.dart';

class JeteTwsSdk {
  Future<String?> getPlatformVersion() {
    return JeteTwsSdkPlatform.instance.getPlatformVersion();
  }
}
