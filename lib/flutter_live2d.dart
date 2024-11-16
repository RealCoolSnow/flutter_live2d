import 'flutter_live2d_platform_interface.dart';

class FlutterLive2d {
  static Future<void> initLive2d() {
    return FlutterLive2dPlatform.instance.initLive2d();
  }

  static Future<void> loadModel(String modelPath) {
    return FlutterLive2dPlatform.instance.loadModel(modelPath);
  }

  static Future<void> setScale(double scale) {
    return FlutterLive2dPlatform.instance.setScale(scale);
  }

  static Future<void> setPosition(double x, double y) {
    return FlutterLive2dPlatform.instance.setPosition(x, y);
  }

  static Future<void> startMotion(String group, int index) {
    return FlutterLive2dPlatform.instance.startMotion(group, index);
  }

  static Future<void> setExpression(String expression) {
    return FlutterLive2dPlatform.instance.setExpression(expression);
  }
}
