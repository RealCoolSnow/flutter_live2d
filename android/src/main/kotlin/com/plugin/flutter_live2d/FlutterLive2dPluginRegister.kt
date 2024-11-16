package com.plugin.flutter_live2d

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.PluginRegistry

object FlutterLive2dPluginRegister {
    fun registerWith(registry: PluginRegistry) {
        println("FlutterLive2dPluginRegister: Registering plugin")
        if (!registry.hasPlugin("com.plugin.flutter_live2d.FlutterLive2dPlugin")) {
            FlutterLive2dPlugin().apply {
                registry.registrarFor("com.plugin.flutter_live2d.FlutterLive2dPlugin")
                    .platformViewRegistry()
                    .registerViewFactory("live2d_view", Live2DViewFactory())
            }
        }
    }
} 