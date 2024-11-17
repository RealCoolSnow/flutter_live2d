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

  /// 设置背景图
  static Future<void> setBackgroundImage(String imagePath) {
    return FlutterLive2dPlatform.instance.setBackgroundImage(imagePath);
  }

  /// 重置模型位置和缩放
  static Future<void> resetModel() async {
    await setScale(1.0);
    await setPosition(0.0, 0.0);
  }

  /// 播放随机动作
  static Future<void> startRandomMotion(String group) async {
    // 默认使用索引0
    await startMotion(group, 0);
  }

  /// 设置模型透明度
  static Future<void> setOpacity(double opacity) {
    return FlutterLive2dPlatform.instance.setOpacity(opacity);
  }

  /// 获取模型是否加载完成
  static Future<bool> isModelLoaded() {
    return FlutterLive2dPlatform.instance.isModelLoaded();
  }
}
