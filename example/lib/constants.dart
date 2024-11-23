import 'package:flutter_live2d/flutter_live2d.dart';

class Live2DAssets {
  static const String MODEL_PATH = 'assets/live2d/Mao/Mao.model3.json';
  static const String BACKGROUND_IMAGE = 'assets/live2d/back_class_normal.png';
}

// 使用顶级类定义的常量
class MotionGroups {
  static const String IDLE = MotionGroup.IDLE;
  static const String TAP_BODY = MotionGroup.TAP_BODY;
  static const String PINCH_IN = MotionGroup.PINCH_IN;
  static const String PINCH_OUT = MotionGroup.PINCH_OUT;
  static const String SHAKE = MotionGroup.SHAKE;
}
