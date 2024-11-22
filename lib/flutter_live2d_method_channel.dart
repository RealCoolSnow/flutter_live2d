import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_live2d_platform_interface.dart';

/// An implementation of [FlutterLive2dPlatform] that uses method channels.
class MethodChannelFlutterLive2d extends FlutterLive2dPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_live2d');

  @override
  Future<void> initLive2d() async {
    await methodChannel.invokeMethod('initLive2d');
  }

  @override
  Future<void> loadModel(String modelPath) async {
    await methodChannel.invokeMethod('loadModel', {'modelPath': modelPath});
  }

  @override
  Future<void> setScale(double scale) async {
    await methodChannel.invokeMethod('setScale', {'scale': scale});
  }

  @override
  Future<void> setPosition(double x, double y) async {
    await methodChannel.invokeMethod('setPosition', {'x': x, 'y': y});
  }

  @override
  Future<void> setOpacity(double opacity) async {
    await methodChannel.invokeMethod('setOpacity', {'opacity': opacity});
  }

  @override
  Future<void> startMotion(String group, int index, {int? priority}) async {
    await methodChannel.invokeMethod('startMotion', {
      'group': group,
      'index': index,
      'priority': priority ?? 2,
    });
  }

  @override
  Future<void> startRandomMotion(String group, {int priority = 2}) async {
    await methodChannel.invokeMethod('startRandomMotion', {
      'group': group,
      'priority': priority,
    });
  }

  @override
  Future<void> setExpression(String expression) async {
    await methodChannel
        .invokeMethod('setExpression', {'expression': expression});
  }

  @override
  Future<void> setRandomExpression() async {
    await methodChannel.invokeMethod('setRandomExpression');
  }

  @override
  Future<bool> isModelLoaded() async {
    final result = await methodChannel.invokeMethod<bool>('isModelLoaded');
    return result ?? false;
  }

  @override
  Future<bool> isMotionFinished() async {
    final result = await methodChannel.invokeMethod<bool>('isMotionFinished');
    return result ?? true;
  }

  @override
  Future<void> setBackgroundImage(String imagePath) async {
    await methodChannel
        .invokeMethod('setBackgroundImage', {'imagePath': imagePath});
  }

  @override
  Future<void> setRenderingTarget(String target) async {
    await methodChannel.invokeMethod('setRenderingTarget', {'target': target});
  }
}
