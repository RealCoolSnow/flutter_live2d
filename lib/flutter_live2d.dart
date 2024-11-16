import 'dart:async';
import 'package:flutter/services.dart';

class FlutterLive2d {
  static const MethodChannel _channel = MethodChannel('flutter_live2d');

  // 初始化Live2D
  static Future<void> initLive2d() async {
    await _channel.invokeMethod('initLive2d');
  }

  // 加载模型
  static Future<void> loadModel(String modelPath) async {
    await _channel.invokeMethod('loadModel', {'modelPath': modelPath});
  }

  // 设置模型缩放
  static Future<void> setScale(double scale) async {
    await _channel.invokeMethod('setScale', {'scale': scale});
  }

  // 设置模型位置
  static Future<void> setPosition(double x, double y) async {
    await _channel.invokeMethod('setPosition', {'x': x, 'y': y});
  }

  // 触发动作
  static Future<void> startMotion(String group, int index) async {
    await _channel.invokeMethod('startMotion', {
      'group': group,
      'index': index,
    });
  }

  // 触发表情
  static Future<void> setExpression(String expression) async {
    await _channel.invokeMethod('setExpression', {'expression': expression});
  }
}
