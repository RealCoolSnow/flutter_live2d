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
    private var touchManager = TouchManager()
    private var viewMatrix = CubismViewMatrix()
    private var deviceToScreen = CubismMatrix44.create()
    private var windowWidth = 0
    private var windowHeight = 0

    // 在 LAppDelegate 中添加
    fun getLAppLive2DManager(): LAppLive2DManager? = live2dManager

    private constructor() {
        // 初始化Cubism SDK
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
        println("LAppDelegate: onStart")
        this.context = context
        live2dManager = LAppLive2DManager()
        
        // 设置渲染目标为默认
        (context as? LAppView)?.switchRenderingTarget(LAppView.RenderingTarget.NONE)
    }

    fun onStop() {
        println("LAppDelegate: onStop")
        live2dManager = null
        CubismFramework.dispose()
    }

    fun onSurfaceCreated() {
        println("LAppDelegate: onSurfaceCreated")
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

        // 初始化View的着色器
        view?.onSurfaceCreated()
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        println("LAppDelegate: onSurfaceChanged width: $width, height: $height")
        windowWidth = width
        windowHeight = height
        
        // 设置视口
        GLES20.glViewport(0, 0, width, height)

        // 设置视图矩阵
        val ratio = width.toFloat() / height.toFloat()
        val left = -ratio
        val right = ratio
        val bottom = -1.0f
        val top = 1.0f

        viewMatrix.setScreenRect(left, right, bottom, top)
        viewMatrix.scale(1.0f, 1.0f)
        viewMatrix.setMaxScale(2.0f)
        viewMatrix.setMinScale(0.8f)
        viewMatrix.setMaxScreenRect(-2.0f, 2.0f, -2.0f, 2.0f)

        // 初始化设备到屏幕的矩阵
        deviceToScreen.loadIdentity()
        if (width > height) {
            val screenW = Math.abs(right - left)
            deviceToScreen.scaleRelative(screenW / width, -screenW / width)
        } else {
            val screenH = Math.abs(top - bottom)
            deviceToScreen.scaleRelative(screenH / height, -screenH / height)
        }
        deviceToScreen.translateRelative(-width * 0.5f, -height * 0.5f)

        // 更新管理器
        live2dManager?.onSurfaceChanged(width, height)
    }

    fun run() {
        // 清除缓冲区
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        // 检查OpenGL错误
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            println("LAppDelegate: OpenGL error: $error")
        }
        
        // 更新和绘制模型
        live2dManager?.onUpdate()
    }

    fun loadModel(modelPath: String) {
        println("LAppDelegate: loadModel path: $modelPath")
        context?.let { ctx ->
            live2dManager?.loadModel(ctx, modelPath)
        }
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
        val screenX = deviceToScreen.transformX(deviceX)
        return viewMatrix.invertTransformX(screenX)
    }

    private fun transformViewY(deviceY: Float): Float {
        val screenY = deviceToScreen.transformY(windowHeight - deviceY)
        return viewMatrix.invertTransformY(screenY)
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

    fun isModelLoaded(): Boolean {
        return live2dManager?.isModelLoaded() ?: false
    }

    fun getContext(): Context? = context
    fun getWindowWidth(): Int = windowWidth
    fun getWindowHeight(): Int = windowHeight

    fun setBackgroundImage(imagePath: String) {
        (context as? LAppView)?.setBackgroundImage(imagePath)
    }

    fun setView(view: LAppView) {
        this.view = view
    }
} 