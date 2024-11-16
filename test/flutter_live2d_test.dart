import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_live2d/flutter_live2d.dart';
import 'package:flutter_live2d/flutter_live2d_platform_interface.dart';
import 'package:flutter_live2d/flutter_live2d_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutterLive2dPlatform
    with MockPlatformInterfaceMixin
    implements FlutterLive2dPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final FlutterLive2dPlatform initialPlatform = FlutterLive2dPlatform.instance;

  test('$MethodChannelFlutterLive2d is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutterLive2d>());
  });

  test('getPlatformVersion', () async {
    FlutterLive2d flutterLive2dPlugin = FlutterLive2d();
    MockFlutterLive2dPlatform fakePlatform = MockFlutterLive2dPlatform();
    FlutterLive2dPlatform.instance = fakePlatform;

    expect(await flutterLive2dPlugin.getPlatformVersion(), '42');
  });
}
