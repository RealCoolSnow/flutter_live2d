
import 'flutter_live2d_platform_interface.dart';

class FlutterLive2d {
  Future<String?> getPlatformVersion() {
    return FlutterLive2dPlatform.instance.getPlatformVersion();
  }
}
