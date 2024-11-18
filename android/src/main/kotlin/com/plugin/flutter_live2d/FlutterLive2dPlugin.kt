package com.plugin.flutter_live2d

import android.content.Context
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class FlutterLive2dPlugin: FlutterPlugin, MethodCallHandler {
    private lateinit var channel : MethodChannel
    private lateinit var context: Context

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_live2d")
        channel.setMethodCallHandler(this)

        flutterPluginBinding.platformViewRegistry.registerViewFactory(
            "live2d_view",
            LAppViewFactory(flutterPluginBinding.binaryMessenger)
        )
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "initLive2d" -> {
                LAppDelegate.getInstance().onStart(context)
                result.success(null)
            }
            "loadModel" -> {
                val modelPath = call.argument<String>("modelPath")
                if (modelPath != null) {
                    try {
                        LAppDelegate.getInstance().loadModel(modelPath)
                        result.success(null)
                    } catch (e: Exception) {
                        result.error("LOAD_ERROR", "Failed to load model", e.message)
                    }
                } else {
                    result.error("INVALID_PATH", "Model path is null", null)
                }
            }
            "setScale" -> {
                val scale = call.argument<Double>("scale")?.toFloat() ?: 1.0f
                LAppDelegate.getInstance().setScale(scale)
                result.success(null)
            }
            "setPosition" -> {
                val x = call.argument<Double>("x")?.toFloat() ?: 0.0f
                val y = call.argument<Double>("y")?.toFloat() ?: 0.0f
                LAppDelegate.getInstance().setPosition(x, y)
                result.success(null)
            }
            "startMotion" -> {
                val group = call.argument<String>("group") ?: return
                val index = call.argument<Int>("index") ?: return
                LAppDelegate.getInstance().startMotion(group, index)
                result.success(null)
            }
            "setExpression" -> {
                val expression = call.argument<String>("expression") ?: return
                LAppDelegate.getInstance().setExpression(expression)
                result.success(null)
            }
            "setBackgroundImage" -> {
                val imagePath = call.argument<String>("imagePath")
                if (imagePath != null) {
                    try {
                        LAppDelegate.getInstance().setBackgroundImage(imagePath)
                        result.success(null)
                    } catch (e: Exception) {
                        result.error("SET_BG_ERROR", "Failed to set background image", e.message)
                    }
                } else {
                    result.error("INVALID_PATH", "Image path is null", null)
                }
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}