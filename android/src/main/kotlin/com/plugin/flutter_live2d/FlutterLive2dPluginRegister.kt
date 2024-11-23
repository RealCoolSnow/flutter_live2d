package com.plugin.flutter_live2d

import android.opengl.GLSurfaceView
import io.flutter.plugin.common.PluginRegistry

object FlutterLive2dPluginRegister {
    fun registerWith(registry: PluginRegistry) {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("Registering Flutter Live2D Plugin")
        }

        if (!registry.hasPlugin("com.plugin.flutter_live2d.FlutterLive2dPlugin")) {
            val registrar = registry.registrarFor("com.plugin.flutter_live2d.FlutterLive2dPlugin")
            
            // 创建GLSurfaceView和Renderer
            val glSurfaceView = GLSurfaceView(registrar.context()).apply {
                setEGLContextClientVersion(2)  // 使用 OpenGL ES 2.0
                val glRenderer = GLRenderer()
                setRenderer(glRenderer)
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            }

            // 注册插件和视图工厂
            FlutterLive2dPlugin().apply {
                registrar.platformViewRegistry().registerViewFactory(
                    "live2d_view",
                    LAppViewFactory(registrar.messenger())
                )
            }

            // 初始化Live2D
            LAppDelegate.getInstance().onStart(registrar.context())

            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("Flutter Live2D Plugin registered successfully")
            }
        }
    }
}