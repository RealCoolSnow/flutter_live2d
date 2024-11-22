import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter_live2d/flutter_live2d.dart';

// 常量定义
class Live2DAssets {
  static const String MODEL_PATH = 'assets/live2d/Mao/Mao.model3.json';
  static const String BACKGROUND_IMAGE = 'assets/live2d/back_class_normal.png';
}

// 动作组定义
class MotionGroups {
  static const String IDLE = "Idle";
  static const String TAP_BODY = "TapBody";
  static const String PINCH_IN = "PinchIn";
  static const String PINCH_OUT = "PinchOut";
  static const String SHAKE = "Shake";
}

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Live2DDemo(),
      theme: ThemeData(
        useMaterial3: true,
        colorSchemeSeed: Colors.blue,
      ),
    );
  }
}

class Live2DDemo extends StatefulWidget {
  @override
  _Live2DDemoState createState() => _Live2DDemoState();
}

class _Live2DDemoState extends State<Live2DDemo> {
  bool _isModelLoaded = false;
  bool _isMotionPlaying = false;
  double _scale = 1.0;
  String _status = "初始化中...";
  String? _backgroundImage;
  String _renderingTarget = FlutterLive2d.renderTargetNone;

  @override
  void initState() {
    super.initState();
    _initLive2D();
  }

  Future<void> _initLive2D() async {
    try {
      _updateStatus("初始化Live2D引擎...");
      await FlutterLive2d.initLive2d();

      _updateStatus("设置背景...");
      await FlutterLive2d.setBackgroundImage(Live2DAssets.BACKGROUND_IMAGE);
      setState(() => _backgroundImage = Live2DAssets.BACKGROUND_IMAGE);

      _updateStatus("加载模型...");
      await FlutterLive2d.loadModel(Live2DAssets.MODEL_PATH);

      // 等待模型加载完成
      while (!await FlutterLive2d.isModelLoaded()) {
        await Future.delayed(Duration(milliseconds: 100));
      }

      setState(() {
        _isModelLoaded = true;
        _status = "加载完成";
      });

      // 开始播放待机动作
      _startIdleMotion();
    } catch (e, stackTrace) {
      print("Live2D初始化失败: $e\n$stackTrace");
      _updateStatus("加载失败: $e");
    }
  }

  void _updateStatus(String status) {
    if (mounted) {
      setState(() => _status = status);
    }
    print("Live2D状态: $status");
  }

  Future<void> _startIdleMotion() async {
    while (mounted) {
      if (!_isMotionPlaying) {
        await FlutterLive2d.startRandomMotion(
          MotionGroups.IDLE,
          priority: 1,
        );
      }
      await Future.delayed(Duration(seconds: 2));
    }
  }

  Future<void> _handleScale(double scale) async {
    setState(() => _scale = scale);
    await FlutterLive2d.setScale(scale);
  }

  Future<void> _handleMotion(String group, int index) async {
    try {
      setState(() => _isMotionPlaying = true);
      await FlutterLive2d.startMotion(group, index, priority: 2);
      while (!await FlutterLive2d.isMotionFinished()) {
        await Future.delayed(Duration(milliseconds: 100));
      }
      setState(() => _isMotionPlaying = false);
    } catch (e) {
      print("动作执行错误: $e");
      setState(() => _isMotionPlaying = false);
    }
  }

  Future<void> _handleRandomMotion(String group) async {
    try {
      setState(() => _isMotionPlaying = true);
      await FlutterLive2d.startRandomMotion(group, priority: 2);
      while (!await FlutterLive2d.isMotionFinished()) {
        await Future.delayed(Duration(milliseconds: 100));
      }
      setState(() => _isMotionPlaying = false);
    } catch (e) {
      print("随机动作执行错误: $e");
      setState(() => _isMotionPlaying = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Live2D Demo'),
        actions: [
          if (_isModelLoaded) _buildMenuButton(),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            _buildLive2DView(),
            if (_isModelLoaded) _buildControlPanel(),
          ],
        ),
      ),
    );
  }

  Widget _buildMenuButton() {
    return PopupMenuButton<String>(
      onSelected: (value) async {
        switch (value) {
          case 'reset':
            await FlutterLive2d.resetModel();
            setState(() => _scale = 1.0);
            break;
          case 'background':
            final newBackground =
                _backgroundImage == null ? Live2DAssets.BACKGROUND_IMAGE : null;
            if (newBackground != null) {
              await FlutterLive2d.setBackgroundImage(newBackground);
            }
            setState(() => _backgroundImage = newBackground);
            break;
          case 'render_none':
            await FlutterLive2d.setRenderingTarget(
                FlutterLive2d.renderTargetNone);
            setState(() => _renderingTarget = FlutterLive2d.renderTargetNone);
            break;
          case 'render_model':
            await FlutterLive2d.setRenderingTarget(
                FlutterLive2d.renderTargetModelBuffer);
            setState(
                () => _renderingTarget = FlutterLive2d.renderTargetModelBuffer);
            break;
          case 'render_view':
            await FlutterLive2d.setRenderingTarget(
                FlutterLive2d.renderTargetViewBuffer);
            setState(
                () => _renderingTarget = FlutterLive2d.renderTargetViewBuffer);
            break;
        }
      },
      itemBuilder: (context) => [
        PopupMenuItem(value: 'reset', child: Text('重置模型')),
        PopupMenuItem(
          value: 'background',
          child: Text(_backgroundImage == null ? '显示背景' : '隐藏背景'),
        ),
        PopupMenuItem(
          value: 'render_none',
          child: Text('默认渲染'),
          enabled: _renderingTarget != FlutterLive2d.renderTargetNone,
        ),
        PopupMenuItem(
          value: 'render_model',
          child: Text('模型缓冲渲染'),
          enabled: _renderingTarget != FlutterLive2d.renderTargetModelBuffer,
        ),
        PopupMenuItem(
          value: 'render_view',
          child: Text('视图缓冲渲染'),
          enabled: _renderingTarget != FlutterLive2d.renderTargetViewBuffer,
        ),
      ],
    );
  }

  Widget _buildLive2DView() {
    return Expanded(
      child: Container(
        color: Colors.grey[100],
        child: Stack(
          children: [
            AndroidView(
              viewType: 'live2d_view',
              creationParamsCodec: const StandardMessageCodec(),
              hitTestBehavior: PlatformViewHitTestBehavior.opaque,
              gestureRecognizers: {
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
    );
  }

  Widget _buildControlPanel() {
    return Material(
      elevation: 8,
      child: Container(
        color: Colors.grey[50],
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).padding.bottom + 8,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            _buildScaleSlider(),
            _buildMotionControls(),
          ],
        ),
      ),
    );
  }

  Widget _buildScaleSlider() {
    return SizedBox(
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
                onChanged: _isMotionPlaying ? null : _handleScale,
              ),
            ),
            Text(
              '${_scale.toStringAsFixed(1)}x',
              style: TextStyle(fontSize: 13),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMotionControls() {
    return SizedBox(
      height: 64,
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        padding: EdgeInsets.symmetric(horizontal: 16),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            _buildButtonGroup('基础动作', [
              _buildMotionButton(
                  '待机', () => _handleMotion(MotionGroups.IDLE, 0)),
              _buildMotionButton(
                  '摇头', () => _handleMotion(MotionGroups.TAP_BODY, 0)),
              _buildMotionButton(
                  '随机', () => _handleRandomMotion(MotionGroups.TAP_BODY)),
            ]),
            SizedBox(width: 20),
            _buildButtonGroup('特殊动作', [
              _buildMotionButton(
                  '缩小', () => _handleMotion(MotionGroups.PINCH_IN, 0)),
              _buildMotionButton(
                  '放大', () => _handleMotion(MotionGroups.PINCH_OUT, 0)),
              _buildMotionButton(
                  '摇晃', () => _handleMotion(MotionGroups.SHAKE, 0)),
            ]),
          ],
        ),
      ),
    );
  }

  Widget _buildMotionButton(String text, VoidCallback onPressed) {
    return SizedBox(
      height: 36,
      child: ElevatedButton(
        onPressed: _isMotionPlaying ? null : onPressed,
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

  @override
  void dispose() {
    super.dispose();
  }
}
