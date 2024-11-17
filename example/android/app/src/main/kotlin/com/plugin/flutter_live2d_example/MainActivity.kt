package com.plugin.flutter_live2d_example

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        // 手动注册插件
        flutterEngine.plugins.add(com.plugin.flutter_live2d.FlutterLive2dPlugin())
    }
}
