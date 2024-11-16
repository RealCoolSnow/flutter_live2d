package com.plugin.flutter_live2d

import android.content.Context
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class Live2DViewFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    private var currentView: Live2DView? = null

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val view = Live2DPlatformView(context)
        currentView = view.live2DView
        return view
    }

    fun getCurrentView(): Live2DView? = currentView
}

class Live2DPlatformView(private val context: Context) : PlatformView {
    val live2DView: Live2DView = Live2DView(context)

    override fun getView() = live2DView

    override fun dispose() {
        live2DView.cleanup()
    }
} 