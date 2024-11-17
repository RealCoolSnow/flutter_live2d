package com.plugin.flutter_live2d

import android.content.Context
import android.opengl.GLES20
import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.CubismFrameworkConfig.LogLevel
import com.live2d.sdk.cubism.core.ICubismLogger

class Live2DDelegate {
    companion object {
        private var instance: Live2DDelegate? = null
        
        fun getInstance(): Live2DDelegate {
            if (instance == null) {
                instance = Live2DDelegate()
            }
            return instance!!
        }
        
        fun releaseInstance() {
            instance = null
        }
    }

    private var context: Context? = null
    private var live2dManager: Live2DManager? = null
    private var view: Live2DView? = null
    private var touchManager = TouchManager()
    private var windowWidth = 0
    private var windowHeight = 0

    private constructor() {
        // Set up Cubism SDK framework
        val option = CubismFramework.Option()
        option.logFunction = object : ICubismLogger {
            override fun print(message: String) {
                println("Live2D: $message")
            }
        }
        option.loggingLevel = LogLevel.VERBOSE

        CubismFramework.cleanUp()
        CubismFramework.startUp(option)
    }

    fun onStart(context: Context) {
        this.context = context
        live2dManager = Live2DManager()
    }

    fun onStop() {
        live2dManager = null
        CubismFramework.dispose()
    }

    fun onDestroy() {
        releaseInstance()
    }

    fun onSurfaceCreated() {
        // 设置纹理采样
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)

        // 设置混合
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Initialize Cubism SDK
        CubismFramework.initialize()
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        // 设置视口
        GLES20.glViewport(0, 0, width, height)
        windowWidth = width
        windowHeight = height

        // Initialize view
        live2dManager?.onSurfaceChanged(width, height)
    }

    fun run() {
        // 清除屏幕
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glClearDepthf(1.0f)

        live2dManager?.onUpdate()
    }

    fun onTouchBegan(x: Float, y: Float) {
        touchManager.touchesBegan(x, y)
        val viewX = transformViewX(touchManager.lastX)
        val viewY = transformViewY(touchManager.lastY)
        live2dManager?.onDrag(viewX, viewY)
    }

    fun onTouchEnd(x: Float, y: Float) {
        val viewX = transformViewX(x)
        val viewY = transformViewY(y)
        
        // 检查是否是点击事件
        if (touchManager.getFlickDistance() < 5.0f) {
            live2dManager?.onTap(viewX, viewY)
        }
        
        live2dManager?.onDrag(0.0f, 0.0f)
    }

    fun onTouchMoved(x: Float, y: Float) {
        touchManager.touchesMoved(x, y)
        val viewX = transformViewX(touchManager.lastX)
        val viewY = transformViewY(touchManager.lastY)
        live2dManager?.onDrag(viewX, viewY)
    }

    private fun transformViewX(deviceX: Float): Float {
        val screenX = live2dManager?.getDeviceToScreenMatrix()?.transformX(deviceX) ?: deviceX
        return live2dManager?.getViewMatrix()?.invertTransformX(screenX) ?: deviceX
    }

    private fun transformViewY(deviceY: Float): Float {
        // 在Android中，Y坐标需要翻转
        val screenY = live2dManager?.getDeviceToScreenMatrix()?.transformY(windowHeight - deviceY) ?: deviceY
        return live2dManager?.getViewMatrix()?.invertTransformY(screenY) ?: deviceY
    }

    fun loadModel(modelPath: String) {
        context?.let { context ->
            live2dManager?.loadModel(context, modelPath)
        }
    }

    fun setScale(scale: Float) {
        live2dManager?.setScale(scale)
    }

    fun setPosition(x: Float, y: Float) {
        live2dManager?.setPosition(x, y)
    }

    fun startMotion(group: String, index: Int) {
        live2dManager?.startMotion(group, index)
    }

    fun setExpression(expression: String) {
        live2dManager?.setExpression(expression)
    }

    fun setOpacity(opacity: Float) {
        live2dManager?.setOpacity(opacity)
    }

    fun isModelLoaded(): Boolean {
        return live2dManager?.isModelLoaded() ?: false
    }

    fun getContext(): Context? = context
    fun getWindowWidth(): Int = windowWidth
    fun getWindowHeight(): Int = windowHeight
} 