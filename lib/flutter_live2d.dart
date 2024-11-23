import 'flutter_live2d_platform_interface.dart';

// 动作组常量
class MotionGroup {
  static const String IDLE = "Idle";
  static const String TAP_BODY = "TapBody";
  static const String PINCH_IN = "PinchIn";
  static const String PINCH_OUT = "PinchOut";
  static const String SHAKE = "Shake";
}

// 优先级常量
class Priority {
  static const int NONE = 0;
  static const int IDLE = 1;
  static const int NORMAL = 2;
  static const int FORCE = 3;
}

// 点击区域常量
class HitArea {
  static const String HEAD = "Head";
  static const String BODY = "Body";
}

class FlutterLive2d {
  // 基础功能
  static Future<void> initLive2d() {
    return FlutterLive2dPlatform.instance.initLive2d();
  }

  static Future<void> loadModel(String modelPath) {
    return FlutterLive2dPlatform.instance.loadModel(modelPath);
  }

  // 模型变换
  static Future<void> setScale(double scale) {
    return FlutterLive2dPlatform.instance.setScale(scale);
  }

  static Future<void> setPosition(double x, double y) {
    return FlutterLive2dPlatform.instance.setPosition(x, y);
  }

  static Future<void> setOpacity(double opacity) {
    return FlutterLive2dPlatform.instance.setOpacity(opacity);
  }

  // 动作和表情
  static Future<void> startMotion(String group, int index, {int? priority}) {
    return FlutterLive2dPlatform.instance
        .startMotion(group, index, priority: priority);
  }

  static Future<void> startRandomMotion(String group, {int priority = 2}) {
    return FlutterLive2dPlatform.instance
        .startRandomMotion(group, priority: priority);
  }

  static Future<void> setExpression(String expression) {
    return FlutterLive2dPlatform.instance.setExpression(expression);
  }

  static Future<void> setRandomExpression() {
    return FlutterLive2dPlatform.instance.setRandomExpression();
  }

  // 状态查询
  static Future<bool> isModelLoaded() {
    return FlutterLive2dPlatform.instance.isModelLoaded();
  }

  static Future<bool> isMotionFinished() {
    return FlutterLive2dPlatform.instance.isMotionFinished();
  }

  // 视图控制
  static Future<void> setBackgroundImage(String imagePath) {
    return FlutterLive2dPlatform.instance.setBackgroundImage(imagePath);
  }

  static Future<void> setRenderingTarget(String target) {
    return FlutterLive2dPlatform.instance.setRenderingTarget(target);
  }

  // 便捷方法
  static Future<void> resetModel() async {
    await setScale(1.0);
    await setPosition(0.0, 0.0);
  }

  // 渲染目标常量
  static const String renderTargetNone = 'NONE';
  static const String renderTargetModelBuffer = 'MODEL_FRAME_BUFFER';
  static const String renderTargetViewBuffer = 'VIEW_FRAME_BUFFER';
}
