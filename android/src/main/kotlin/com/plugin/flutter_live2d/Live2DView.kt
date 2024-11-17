package com.plugin.flutter_live2d

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.CubismFrameworkConfig.LogLevel
import com.live2d.sdk.cubism.core.ICubismLogger

class Live2DView(context: Context) : GLSurfaceView(context) {
    private var delegate: Live2DDelegate = Live2DDelegate.getInstance()
    private val renderer: GLRenderer

    init {
        println("Live2DView: Initializing GLSurfaceView...")
        
        // 设置OpenGL ES 2.0
        setEGLContextClientVersion(2)
        
        // 设置透明背景
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        
        // 设置渲染器
        renderer = GLRenderer()
        setRenderer(renderer)
        
        // 设置渲染模式为连续渲染
        renderMode = RENDERMODE_CONTINUOUSLY

        // 初始化Live2D
        delegate.onStart(context)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointX = event.x
        val pointY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                delegate.onTouchBegan(pointX, pointY)
            }
            MotionEvent.ACTION_UP -> {
                delegate.onTouchEnd(pointX, pointY)
            }
            MotionEvent.ACTION_MOVE -> {
                delegate.onTouchMoved(pointX, pointY)
            }
        }
        return true
    }

    fun dispose() {
        println("Live2DView: Disposing...")
        delegate.onStop()
    }
} 