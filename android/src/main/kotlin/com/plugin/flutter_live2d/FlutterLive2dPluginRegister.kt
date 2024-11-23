package com.plugin.flutter_live2d

import io.flutter.plugin.common.PluginRegistry

object FlutterLive2dPluginRegister {
    fun registerWith(registry: PluginRegistry) {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("Registering Flutter Live2D Plugin")
        }

        if (!registry.hasPlugin("com.plugin.flutter_live2d.FlutterLive2dPlugin")) {
            val registrar = registry.registrarFor("com.plugin.flutter_live2d.FlutterLive2dPlugin")
            
            // 注册插件和视图工厂
            FlutterLive2dPlugin().apply {
                registrar.platformViewRegistry().registerViewFactory(
                    "live2d_view",
                    LAppViewFactory(registrar.messenger())
                )
            }

            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("Flutter Live2D Plugin registered successfully")
            }
        }
    }
}