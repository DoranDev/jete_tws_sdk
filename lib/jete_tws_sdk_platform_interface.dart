import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'jete_tws_sdk_method_channel.dart';

abstract class JeteTwsSdkPlatform extends PlatformInterface {
  /// Constructs a JeteTwsSdkPlatform.
  JeteTwsSdkPlatform() : super(token: _token);

  static final Object _token = Object();

  static JeteTwsSdkPlatform _instance = MethodChannelJeteTwsSdk();

  /// The default instance of [JeteTwsSdkPlatform] to use.
  ///
  /// Defaults to [MethodChannelJeteTwsSdk].
  static JeteTwsSdkPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [JeteTwsSdkPlatform] when
  /// they register themselves.
  static set instance(JeteTwsSdkPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
