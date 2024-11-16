package com.example.flutter_live2d

import android.content.Context
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class Live2DViewFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        return Live2DPlatformView(context)
    }
}

class Live2DPlatformView(private val context: Context) : PlatformView {
    private val live2DView: Live2DView = Live2DView(context)

    override fun getView() = live2DView

    override fun dispose() {
        // 清理资源
    }
} 