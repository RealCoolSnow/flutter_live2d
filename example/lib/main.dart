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
    print("Live2DDemo: initState");
    WidgetsBinding.instance.addPostFrameCallback((_) {
      print("Live2DDemo: postFrameCallback");
      _initLive2D();
    });
  }

  Future<void> _initLive2D() async {
    try {
      print("Live2DDemo: Starting initialization");
      await FlutterLive2d.initLive2d();
      print("Live2DDemo: Live2D initialized");

      await Future.delayed(Duration(milliseconds: 500));
      print("Live2DDemo: Delay completed");

      await FlutterLive2d.loadModel("assets/live2d/Haru/Haru.model3.json");
      print("Live2DDemo: Model loaded");
    } catch (e, stackTrace) {
      print("Live2DDemo: Initialization failed");
      print("Live2DDemo: Error: $e");
      print("Live2DDemo: Stack trace: $stackTrace");
    }
  }

  @override
  Widget build(BuildContext context) {
    print("Live2DDemo: Building widget");
    return Scaffold(
      appBar: AppBar(title: Text('Live2D Demo')),
      body: Stack(
        children: [
          // Live2D视图
          Center(
            child: Container(
              width: MediaQuery.of(context).size.width,
              height: MediaQuery.of(context).size.height * 0.8,
              color: Colors.grey[200],
              child: AndroidView(
                viewType: 'live2d_view',
                creationParams: <String, dynamic>{},
                creationParamsCodec: const StandardMessageCodec(),
                onPlatformViewCreated: (int id) {
                  print("Live2DDemo: Platform view created with id: $id");
                },
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
