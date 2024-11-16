import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_live2d_platform_interface.dart';

/// An implementation of [FlutterLive2dPlatform] that uses method channels.
class MethodChannelFlutterLive2d extends FlutterLive2dPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_live2d');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
