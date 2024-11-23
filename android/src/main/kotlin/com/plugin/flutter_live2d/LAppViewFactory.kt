package com.plugin.flutter_live2d

import android.content.Context
import android.view.View
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import android.opengl.GLSurfaceView

class LAppViewFactory(
    private val messenger: BinaryMessenger
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("Creating Live2D view with id: $viewId")
        }
        
        // 创建GLSurfaceView并设置渲染器
        val glSurfaceView = GLSurfaceView(context).apply {
            setEGLContextClientVersion(2)
            setRenderer(GLRenderer())
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        // 初始化Live2D
        LAppDelegate.getInstance().apply {
            onStart(context)
            onSurfaceCreated()
        }

        return Live2DPlatformView(glSurfaceView, viewId)
    }
}

class Live2DPlatformView(
    private val glSurfaceView: GLSurfaceView,
    private val viewId: Int
) : PlatformView {

    override fun getView(): View = glSurfaceView

    override fun dispose() {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("Disposing Live2D view $viewId")
        }
        glSurfaceView.onPause()
        LAppDelegate.getInstance().onDestroy()
    }
}