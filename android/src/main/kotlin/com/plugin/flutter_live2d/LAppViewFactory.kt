package com.plugin.flutter_live2d

import android.content.Context
import android.view.View
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class LAppViewFactory(
    private val messenger: BinaryMessenger
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("Creating Live2D view with id: $viewId")
        }
        return Live2DPlatformView(context, viewId)
    }
}

class Live2DPlatformView(
    private val context: Context,
    private val viewId: Int
) : PlatformView {
    private val view: LAppView

    init {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("Initializing Live2D view $viewId")
        }
        
        // 创建并初始化视图
        view = LAppView(context).apply {
            initialize()
            initializeSprite()
        }
    }

    override fun getView(): View = view

    override fun dispose() {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("Disposing Live2D view $viewId")
        }
        view.close() // 使用新的 AutoCloseable 接口
    }
}