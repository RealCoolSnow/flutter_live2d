package com.plugin.flutter_live2d

import android.content.Context
import android.view.View
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class Live2DViewFactory(
    private val messenger: BinaryMessenger
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        println("Live2DViewFactory: Creating view with id: $viewId")
        return Live2DPlatformView(context, viewId)
    }
}

class Live2DPlatformView(
    private val context: Context,
    private val viewId: Int
) : PlatformView {
    private val view: Live2DView = Live2DView(context)

    init {
        println("Live2DPlatformView: Initializing view $viewId")
    }

    override fun getView(): View = view

    override fun dispose() {
        println("Live2DPlatformView: Disposing view $viewId")
        view.dispose()
    }
} 