import 'package:flutter/material.dart';
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
  @override
  void initState() {
    super.initState();
    _initLive2D();
  }

  Future<void> _initLive2D() async {
    try {
      await FlutterLive2d.initLive2d();
      // 假设模型文件放在assets/live2d/model.model3.json
      await FlutterLive2d.loadModel("assets/live2d/model.model3.json");
    } catch (e) {
      print("Live2D初始化失败: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Live2D Demo')),
      body: Stack(
        children: [
          // Live2D视图
          Center(
            child: Container(
              width: 300,
              height: 400,
              child: AndroidView(
                viewType: 'live2d_view',
                creationParams: <String, dynamic>{},
                creationParamsCodec: const StandardMessageCodec(),
              ),
            ),
          ),

          // 控制按钮
          Positioned(
            bottom: 20,
            left: 0,
            right: 0,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: () => FlutterLive2d.startMotion("idle", 0),
                  child: Text('待机动作'),
                ),
                ElevatedButton(
                  onPressed: () => FlutterLive2d.setExpression("smile"),
                  child: Text('微笑表情'),
                ),
                ElevatedButton(
                  onPressed: () => FlutterLive2d.setScale(1.5),
                  child: Text('放大'),
                ),
                ElevatedButton(
                  onPressed: () => FlutterLive2d.setScale(1.0),
                  child: Text('还原'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
