package com.plugin.flutter_live2d

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.CubismFrameworkConfig.LogLevel
import com.live2d.sdk.cubism.core.ICubismLogger
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Live2DView(context: Context) : GLSurfaceView(context), GLSurfaceView.Renderer {
    private var delegate: Live2DDelegate = Live2DDelegate.getInstance()

    init {
        println("Live2DView: Initializing GLSurfaceView...")
        
        // 设置OpenGL ES 2.0
        setEGLContextClientVersion(2)
        
        // 设置透明背景
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        
        // 设置渲染器
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY

        // 初始化Live2D
        delegate.onStart(context)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        println("Live2DView: Surface created")
        // 设置浅蓝色背景，方便调试
        GLES20.glClearColor(0.9f, 0.9f, 1.0f, 1.0f)
        delegate.onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        println("Live2DView: Surface changed: $width x $height")
        GLES20.glViewport(0, 0, width, height)
        delegate.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // 清除缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        println("Live2DView: Drawing frame")
        delegate.run()
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