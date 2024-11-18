package com.plugin.flutter_live2d

import android.content.Context
import android.opengl.GLES20
import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.CubismFrameworkConfig.LogLevel
import com.live2d.sdk.cubism.core.ICubismLogger
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.math.CubismViewMatrix

class LAppDelegate {
    companion object {
        private var instance: LAppDelegate? = null
        
        fun getInstance(): LAppDelegate {
            if (instance == null) {
                instance = LAppDelegate()
            }
            return instance!!
        }

        fun releaseInstance() {
            instance = null
        }
    }

    private var context: Context? = null
    private var live2dManager: LAppLive2DManager? = null
    private var view: LAppView? = null
    private var textureManager: LAppTextureManager? = null
    private var isActive = true
    private var windowWidth = 0
    private var windowHeight = 0
    private var mouseX = 0f
    private var mouseY = 0f
    private var isCaptured = false

    fun onStart(context: Context) {
        this.context = context
        textureManager = LAppTextureManager(context)
        live2dManager = LAppLive2DManager.getInstance(context)
        
        // 更新时间
        LAppPal.updateTime()
    }

    fun onPause() {
        // 保存当前状态
    }

    fun onStop() {
        view?.dispose()
        textureManager = null
        live2dManager = null
        CubismFramework.dispose()
    }

    fun onDestroy() {
        releaseInstance()
    }

    fun onSurfaceCreated() {
        // 设置OpenGL状态
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFuncSeparate(
            GLES20.GL_SRC_ALPHA,
            GLES20.GL_ONE_MINUS_SRC_ALPHA,
            GLES20.GL_ONE,
            GLES20.GL_ONE_MINUS_SRC_ALPHA
        )
        
        // 初始化Cubism SDK
        CubismFramework.initialize()
        
        // 初始化View
        view?.onSurfaceCreated()
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        windowWidth = width
        windowHeight = height
        GLES20.glViewport(0, 0, width, height)
        
        view?.initialize()
        view?.initializeSprite()
        
        isActive = true
    }

    fun run() {
        // 更新时间
        LAppPal.updateTime()

        // 画面初始化
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glClearDepthf(1.0f)

        // 更新和渲染
        view?.render()

        if (!isActive) {
            // 处理非活动状态
        }
    }

    fun setView(view: LAppView) {
        this.view = view
    }

    fun getWindowWidth(): Int = windowWidth
    fun getWindowHeight(): Int = windowHeight

    fun onTouchBegan(x: Float, y: Float) {
        println("LAppDelegate: onTouchBegan x:$x y:$y")
        mouseX = x
        mouseY = y
        isCaptured = true
        live2dManager?.onDrag(mouseX, mouseY)
    }

    fun onTouchEnd(x: Float, y: Float) {
        println("LAppDelegate: onTouchEnd x:$x y:$y")
        mouseX = x
        mouseY = y
        isCaptured = false
        live2dManager?.onDrag(0f, 0f)
        live2dManager?.onTap(x, y)
    }

    fun onTouchMoved(x: Float, y: Float) {
        println("LAppDelegate: onTouchMoved x:$x y:$y")
        mouseX = x
        mouseY = y
        if (isCaptured) {
            live2dManager?.onDrag(mouseX, mouseY)
        }
    }

    fun loadModel(modelPath: String) {
        println("LAppDelegate: Loading model from $modelPath")
        live2dManager?.loadModel(modelPath)
    }

    fun setScale(scale: Float) {
        println("LAppDelegate: Setting scale to $scale")
        live2dManager?.setScale(scale)
    }

    fun setPosition(x: Float, y: Float) {
        println("LAppDelegate: Setting position to x:$x y:$y")
        live2dManager?.setPosition(x, y)
    }

    fun startMotion(group: String, index: Int) {
        println("LAppDelegate: Starting motion group:$group index:$index")
        live2dManager?.startMotion(group, index)
    }

    fun setExpression(expression: String) {
        println("LAppDelegate: Setting expression: $expression")
        live2dManager?.setExpression(expression)
    }

    fun setBackgroundImage(imagePath: String) {
        println("LAppDelegate: Setting background image: $imagePath")
        view?.setBackgroundImage(imagePath)
    }
} 