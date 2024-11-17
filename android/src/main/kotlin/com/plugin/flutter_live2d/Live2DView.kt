package com.plugin.flutter_live2d

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.view.MotionEvent

class Live2DView(context: Context) : GLSurfaceView(context) {
    private val renderer = GLRenderer()

    init {
        println("Live2DView: Initializing...")
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointX = event.x
        val pointY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Live2DDelegate.getInstance().onTouchBegan(pointX, pointY)
            }
            MotionEvent.ACTION_UP -> {
                Live2DDelegate.getInstance().onTouchEnd(pointX, pointY)
            }
            MotionEvent.ACTION_MOVE -> {
                Live2DDelegate.getInstance().onTouchMoved(pointX, pointY)
            }
        }
        return super.onTouchEvent(event)
    }

    fun dispose() {
        Live2DDelegate.getInstance().onStop()
    }
} 