import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter_live2d/flutter_live2d.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Live2DDemo(),
    );
  }
}

class Live2DDemo extends StatefulWidget {
  @override
  _Live2DDemoState createState() => _Live2DDemoState();
}

class _Live2DDemoState extends State<Live2DDemo> {
  bool _isModelLoaded = false;
  double _scale = 1.0;
  String _status = "初始化中...";

  @override
  void initState() {
    super.initState();
    print("Live2DDemo: initState");
    WidgetsBinding.instance.addPostFrameCallback((_) {
      print("Live2DDemo: postFrameCallback");
      _initLive2D();
    });
  }

  Future<void> _initLive2D() async {
    try {
      setState(() => _status = "初始化Live2D引擎...");
      print("Live2DDemo: Starting initialization");
      await FlutterLive2d.initLive2d();
      print("Live2DDemo: Live2D initialized");

      setState(() => _status = "等待视图准备...");
      await Future.delayed(Duration(seconds: 1));
      print("Live2DDemo: Delay completed");

      setState(() => _status = "加载模型...");
      await FlutterLive2d.loadModel("assets/live2d/Hiyori/Hiyori.model3.json");
      print("Live2DDemo: Model loaded");

      setState(() {
        _isModelLoaded = true;
        _status = "加载完成";
      });
    } catch (e, stackTrace) {
      print("Live2DDemo: Initialization failed");
      print("Live2DDemo: Error: $e");
      print("Live2DDemo: Stack trace: $stackTrace");
      setState(() => _status = "加载失败: $e");
    }
  }

  void _handleScale(double scale) async {
    setState(() => _scale = scale);
    await FlutterLive2d.setScale(scale);
  }

  void _handleMotion(String group, int index) async {
    try {
      await FlutterLive2d.startMotion(group, index);
    } catch (e) {
      print("Motion error: $e");
    }
  }

  void _handleExpression(String expression) async {
    try {
      await FlutterLive2d.setExpression(expression);
    } catch (e) {
      print("Expression error: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    print("Live2DDemo: Building widget");
    return Scaffold(
      appBar: AppBar(
        title: Text('Live2D Demo'),
        actions: [
          if (_isModelLoaded)
            IconButton(
              icon: Icon(Icons.refresh),
              onPressed: () => FlutterLive2d.resetModel(),
            ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            // Live2D视图
            Expanded(
              child: Container(
                color: Colors.blue[50],
                child: Stack(
                  children: [
                    AndroidView(
                      viewType: 'live2d_view',
                      creationParams: <String, dynamic>{},
                      creationParamsCodec: const StandardMessageCodec(),
                      onPlatformViewCreated: (int id) {
                        print("Live2DDemo: Platform view created with id: $id");
                      },
                      hitTestBehavior: PlatformViewHitTestBehavior.opaque,
                      layoutDirection: TextDirection.ltr,
                      gestureRecognizers: <Factory<
                          OneSequenceGestureRecognizer>>{
                        Factory<OneSequenceGestureRecognizer>(
                          () => EagerGestureRecognizer(),
                        ),
                      },
                    ),
                    if (!_isModelLoaded)
                      Center(
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            CircularProgressIndicator(),
                            SizedBox(height: 16),
                            Text(_status),
                          ],
                        ),
                      ),
                  ],
                ),
              ),
            ),

            // 底部控制面板
            if (_isModelLoaded)
              Material(
                elevation: 8,
                child: Container(
                  color: Colors.grey[50],
                  padding: EdgeInsets.only(
                    bottom: MediaQuery.of(context).padding.bottom + 8,
                  ),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      // 缩放滑块
                      SizedBox(
                        height: 40,
                        child: Padding(
                          padding: EdgeInsets.symmetric(horizontal: 16),
                          child: Row(
                            children: [
                              Icon(Icons.zoom_in, size: 18),
                              Expanded(
                                child: Slider(
                                  value: _scale,
                                  min: 0.5,
                                  max: 2.0,
                                  onChanged: _handleScale,
                                ),
                              ),
                              Text('${_scale.toStringAsFixed(1)}x',
                                  style: TextStyle(fontSize: 13)),
                            ],
                          ),
                        ),
                      ),
                      // 动作和表情按钮
                      SizedBox(
                        height: 64,
                        child: SingleChildScrollView(
                          scrollDirection: Axis.horizontal,
                          padding: EdgeInsets.symmetric(horizontal: 16),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              _buildButtonGroup('动作', [
                                _buildButton(
                                    '待机', () => _handleMotion("Idle", 0)),
                                _buildButton(
                                    '摇头', () => _handleMotion("TapBody", 0)),
                                _buildButton(
                                    '随机',
                                    () => FlutterLive2d.startRandomMotion(
                                        "TapBody")),
                              ]),
                              SizedBox(width: 20),
                              _buildButtonGroup('表情', [
                                _buildButton(
                                    '微笑', () => _handleExpression("smile")),
                                _buildButton(
                                    '生气', () => _handleExpression("angry")),
                                _buildButton(
                                    '难过', () => _handleExpression("sad")),
                              ]),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildButton(String text, VoidCallback onPressed) {
    return SizedBox(
      height: 36,
      child: ElevatedButton(
        onPressed: onPressed,
        style: ElevatedButton.styleFrom(
          padding: EdgeInsets.symmetric(horizontal: 16),
          textStyle: TextStyle(fontSize: 14),
          minimumSize: Size.zero,
          tapTargetSize: MaterialTapTargetSize.shrinkWrap,
        ),
        child: Text(text),
      ),
    );
  }

  Widget _buildButtonGroup(String title, List<Widget> buttons) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.bold,
            color: Colors.black87,
          ),
        ),
        SizedBox(height: 4),
        Row(
          mainAxisSize: MainAxisSize.min,
          children: buttons
              .map((button) => Padding(
                    padding: EdgeInsets.only(right: 8),
                    child: button,
                  ))
              .toList(),
        ),
      ],
    );
  }
}
