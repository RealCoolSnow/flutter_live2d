package com.plugin.flutter_live2d

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class FlutterLive2dPlugin: FlutterPlugin, MethodCallHandler {
    private lateinit var channel : MethodChannel
    private var viewFactory: Live2DViewFactory? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_live2d")
        channel.setMethodCallHandler(this)
        
        // 注册Live2DView Factory
        viewFactory = Live2DViewFactory()
        flutterPluginBinding
            .platformViewRegistry
            .registerViewFactory("live2d_view", viewFactory!!)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        val live2DView = viewFactory?.getCurrentView()
        
        when (call.method) {
            "initLive2d" -> {
                // 初始化Live2D
                result.success(null)
            }
            "loadModel" -> {
                val modelPath = call.argument<String>("modelPath")
                modelPath?.let { live2DView?.loadModel(it) }
                result.success(null)
            }
            "setScale" -> {
                val scale = call.argument<Double>("scale")?.toFloat() ?: 1.0f
                live2DView?.setScale(scale)
                result.success(null)
            }
            "setPosition" -> {
                val x = call.argument<Double>("x")?.toFloat() ?: 0.0f
                val y = call.argument<Double>("y")?.toFloat() ?: 0.0f
                live2DView?.setPosition(x, y)
                result.success(null)
            }
            "startMotion" -> {
                val group = call.argument<String>("group") ?: return
                val index = call.argument<Int>("index") ?: return
                live2DView?.startMotion(group, index)
                result.success(null)
            }
            "setExpression" -> {
                val expression = call.argument<String>("expression") ?: return
                live2DView?.setExpression(expression)
                result.success(null)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
} 