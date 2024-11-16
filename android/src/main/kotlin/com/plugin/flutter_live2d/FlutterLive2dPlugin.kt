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
            
            viewFactory = Live2DViewFactory()
            println("FlutterLive2dPlugin: View factory created")
            
            flutterPluginBinding
                .platformViewRegistry
                .registerViewFactory("live2d_view", viewFactory!!)
            println("FlutterLive2dPlugin: View factory registered")
        } catch (e: Exception) {
            println("FlutterLive2dPlugin: Error in onAttachedToEngine")
            e.printStackTrace()
        }
        
        println("FlutterLive2dPlugin: onAttachedToEngine end")
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        val live2DView = viewFactory?.getCurrentView()
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
                if (live2DView == null) {
                    println("FlutterLive2dPlugin: Live2DView is null")
                    result.error("NO_VIEW", "Live2DView not found", null)
                    return
                }
                modelPath?.let { 
                    live2DView.loadModel(it)
                    println("FlutterLive2dPlugin: Model loaded")
                }
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