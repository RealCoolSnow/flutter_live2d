// In order to *not* need this ignore, consider extracting the "web" version
// of your plugin as a separate package, instead of inlining it in the same
// package as the core of your plugin.
// ignore: avoid_web_libraries_in_flutter

import 'package:flutter_web_plugins/flutter_web_plugins.dart';
import 'package:web/web.dart' as web;

import 'flutter_live2d_platform_interface.dart';

/// A web implementation of the FlutterLive2dPlatform of the FlutterLive2d plugin.
class FlutterLive2dWeb extends FlutterLive2dPlatform {
  /// Constructs a FlutterLive2dWeb
  FlutterLive2dWeb();

  static void registerWith(Registrar registrar) {
    FlutterLive2dPlatform.instance = FlutterLive2dWeb();
  }

  /// Returns a [String] containing the version of the platform.
  @override
  Future<String?> getPlatformVersion() async {
    final version = web.window.navigator.userAgent;
    return version;
  }
}
