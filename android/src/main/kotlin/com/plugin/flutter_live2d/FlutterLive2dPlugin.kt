package com.plugin.flutter_live2d

import android.content.Context
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import com.plugin.flutter_live2d.LAppDefine

class FlutterLive2dPlugin: FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_live2d")
        channel.setMethodCallHandler(this)

        flutterPluginBinding.platformViewRegistry.registerViewFactory(
            "live2d_view",
            LAppViewFactory(flutterPluginBinding.binaryMessenger)
        )

        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("Flutter Live2D Plugin attached")
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        try {
            when (call.method) {
                "initLive2d" -> {
                    LAppDelegate.getInstance().apply {
                        onStart(context)
                    }
                    result.success(null)
                }
                "loadModel" -> {
                    val modelPath = call.argument<String>("modelPath")
                        ?: throw IllegalArgumentException("Model path is null")
                    
                    LAppLive2DManager.getInstance().loadModel(modelPath)
                    result.success(null)
                }
                "setScale" -> {
                    val scale = call.argument<Double>("scale")?.toFloat() ?: 1.0f
                    LAppLive2DManager.getInstance().getModel(0)?.setScale(scale)
                    result.success(null)
                }
                "setPosition" -> {
                    val x = call.argument<Double>("x")?.toFloat() ?: 0.0f
                    val y = call.argument<Double>("y")?.toFloat() ?: 0.0f
                    LAppLive2DManager.getInstance().getModel(0)?.setPosition(x, y)
                    result.success(null)
                }
                "startMotion" -> {
                    val group = call.argument<String>("group") 
                        ?: throw IllegalArgumentException("Motion group is null")
                    val index = call.argument<Int>("index") 
                        ?: throw IllegalArgumentException("Motion index is null")
                    val priority = call.argument<Int>("priority") ?: LAppDefine.Priority.NORMAL.priority
                    
                    LAppLive2DManager.getInstance().getModel(0)?.startMotion(
                        group, index, priority
                    )
                    result.success(null)
                }
                "startRandomMotion" -> {
                    val group = call.argument<String>("group")
                        ?: throw IllegalArgumentException("Motion group is null")
                    val priority = call.argument<Int>("priority") ?: LAppDefine.Priority.NORMAL.priority
                    
                    LAppLive2DManager.getInstance().getModel(0)?.startRandomMotion(
                        group, priority
                    )
                    result.success(null)
                }
                "setExpression" -> {
                    val expression = call.argument<String>("expression")
                        ?: throw IllegalArgumentException("Expression is null")
                    
                    LAppLive2DManager.getInstance().getModel(0)?.setExpression(expression)
                    result.success(null)
                }
                "setRandomExpression" -> {
                    LAppLive2DManager.getInstance().getModel(0)?.setRandomExpression()
                    result.success(null)
                }
                "isMotionFinished" -> {
                    val isFinished = LAppLive2DManager.getInstance().getModel(0)?.isMotionFinished() ?: true
                    result.success(isFinished)
                }
                "isModelLoaded" -> {
                    val isLoaded = LAppLive2DManager.getInstance().getModel(0) != null
                    result.success(isLoaded)
                }
                "setRenderingTarget" -> {
                    val target = call.argument<String>("target")
                        ?: throw IllegalArgumentException("Rendering target is null")
                    LAppDelegate.getInstance().getView()?.switchRenderingTarget(
                        LAppView.RenderingTarget.valueOf(target)
                    )
                    result.success(null)
                }
                "setBackgroundImage" -> {
                    val imagePath = call.argument<String>("imagePath")
                        ?: throw IllegalArgumentException("Image path is null")
                    
                    LAppDelegate.getInstance().getView()?.setBackgroundImage(imagePath)
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        } catch (e: Exception) {
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("Error in method call ${call.method}: ${e.message}")
            }
            result.error("LIVE2D_ERROR", e.message, null)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("Flutter Live2D Plugin detached")
        }
        channel.setMethodCallHandler(null)
        LAppDelegate.getInstance().onDestroy()
    }
}