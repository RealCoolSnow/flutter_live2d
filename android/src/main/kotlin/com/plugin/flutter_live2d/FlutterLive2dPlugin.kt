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

    init {
        println("FlutterLive2dPlugin: Constructor called")
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        println("FlutterLive2dPlugin: onAttachedToEngine start")
        
        try {
            channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_live2d")
            println("FlutterLive2dPlugin: Channel created")
            
            channel.setMethodCallHandler(this)
            println("FlutterLive2dPlugin: Method handler set")
            
            flutterPluginBinding.platformViewRegistry
                .registerViewFactory(
                    "live2d_view",
                    Live2DViewFactory(flutterPluginBinding.binaryMessenger)
                )
            println("FlutterLive2dPlugin: View factory registered")
        } catch (e: Exception) {
            println("FlutterLive2dPlugin: Error in onAttachedToEngine")
            e.printStackTrace()
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        println("FlutterLive2dPlugin: Method called: ${call.method}")
        
        when (call.method) {
            "initLive2d" -> {
                println("FlutterLive2dPlugin: Initializing Live2D")
                result.success(null)
            }
            "loadModel" -> {
                println("FlutterLive2dPlugin: Loading model")
                val modelPath = call.argument<String>("modelPath")
                println("FlutterLive2dPlugin: Model path: $modelPath")
                if (modelPath != null) {
                    try {
                        Live2DDelegate.getInstance().loadModel(modelPath)
                        println("FlutterLive2dPlugin: Model load initiated")
                        result.success(null)
                    } catch (e: Exception) {
                        println("FlutterLive2dPlugin: Model load failed")
                        e.printStackTrace()
                        result.error("LOAD_ERROR", "Failed to load model", e.message)
                    }
                } else {
                    result.error("INVALID_PATH", "Model path is null", null)
                }
            }
            "setScale" -> {
                val scale = call.argument<Double>("scale")?.toFloat() ?: 1.0f
                Live2DDelegate.getInstance().setScale(scale)
                result.success(null)
            }
            "setPosition" -> {
                val x = call.argument<Double>("x")?.toFloat() ?: 0.0f
                val y = call.argument<Double>("y")?.toFloat() ?: 0.0f
                Live2DDelegate.getInstance().setPosition(x, y)
                result.success(null)
            }
            "startMotion" -> {
                val group = call.argument<String>("group") ?: return
                val index = call.argument<Int>("index") ?: return
                Live2DDelegate.getInstance().startMotion(group, index)
                result.success(null)
            }
            "setExpression" -> {
                val expression = call.argument<String>("expression") ?: return
                Live2DDelegate.getInstance().setExpression(expression)
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