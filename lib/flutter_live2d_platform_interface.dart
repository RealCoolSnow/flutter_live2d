import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_live2d_method_channel.dart';

abstract class FlutterLive2dPlatform extends PlatformInterface {
  /// Constructs a FlutterLive2dPlatform.
  FlutterLive2dPlatform() : super(token: _token);

  static final Object _token = Object();

  static FlutterLive2dPlatform _instance = MethodChannelFlutterLive2d();

  /// The default instance of [FlutterLive2dPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterLive2d].
  static FlutterLive2dPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [FlutterLive2dPlatform] when
  /// they register themselves.
  static set instance(FlutterLive2dPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
