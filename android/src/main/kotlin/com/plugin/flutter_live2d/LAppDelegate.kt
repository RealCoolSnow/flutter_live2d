package com.plugin.flutter_live2d

import android.content.Context
import android.opengl.GLES20
import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.CubismFrameworkConfig.LogLevel

class LAppDelegate private constructor() {
    companion object {
        private var instance: LAppDelegate? = null
        
        fun getInstance(): LAppDelegate {
            if (instance == null) {
                instance = LAppDelegate()
            }
            return instance!!
        }

        fun releaseInstance() {
            instance?.let {
                it.textureManager = null
                it.view = null
                it.context = null
            }
            instance = null
        }
    }

    // Framework设置选项
    private val cubismOption = CubismFramework.Option().apply {
        logFunction = LAppPal.PrintLogFunction()
        loggingLevel = LAppDefine.cubismLoggingLevel
    }

    private var context: Context? = null
    private var textureManager: LAppTextureManager? = null
    private var view: LAppView? = null
    private var currentModel: Int = 0
    private var windowWidth: Int = 0
    private var windowHeight: Int = 0
    private var isActive: Boolean = true
    private var isCaptured: Boolean = false
    private var mouseX: Float = 0f
    private var mouseY: Float = 0f

    init {
        // 初始化Cubism SDK
        CubismFramework.cleanUp()
        CubismFramework.startUp(cubismOption)
    }

    fun onStart(context: Context) {
        println("LAppDelegate: Starting with context")
        this.context = context
        textureManager = LAppTextureManager(context)
        view = LAppView(context)
        
        // 更新时间
        LAppPal.updateTime()
    }

    fun onPause() {
        println("LAppDelegate: Pausing")
        currentModel = LAppLive2DManager.getInstance(context!!).getCurrentModel()
    }

    fun onStop() {
        println("LAppDelegate: Stopping")
        view?.dispose()
        textureManager = null
        
        LAppLive2DManager.releaseInstance()
        CubismFramework.dispose()
    }

    fun onDestroy() {
        println("LAppDelegate: Destroying")
        releaseInstance()
    }

    fun onSurfaceCreated() {
        println("LAppDelegate: Surface created")
        
        // 纹理采样设置
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)

        // 混合设置
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // 初始化Cubism SDK
        CubismFramework.initialize()
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        println("LAppDelegate: Surface changed to $width x $height")
        
        // 设置视口
        GLES20.glViewport(0, 0, width, height)
        windowWidth = width
        windowHeight = height

        // 初始化视图
        view?.initialize()
        view?.initializeSprite()

        // 加载模型
        val manager = LAppLive2DManager.getInstance(context!!)
        if (manager.getCurrentModel() != currentModel) {
            manager.changeScene(currentModel)
        }

        isActive = true
    }

    fun run() {
        // 更新时间
        LAppPal.updateTime()

        // 清除画面
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glClearDepthf(1.0f)

        // 渲染
        view?.render()

        // 检查活动状态
        if (!isActive) {
            deactivateApp()
        }
    }

    fun onTouchBegan(x: Float, y: Float) {
        println("LAppDelegate: Touch began at ($x, $y)")
        mouseX = x
        mouseY = y
        
        view?.let {
            isCaptured = true
            it.onTouchesBegan(mouseX, mouseY)
        }
    }

    fun onTouchEnd(x: Float, y: Float) {
        println("LAppDelegate: Touch ended at ($x, $y)")
        mouseX = x
        mouseY = y
        
        view?.let {
            isCaptured = false
            it.onTouchesEnded(mouseX, mouseY)
        }
    }

    fun onTouchMoved(x: Float, y: Float) {
        println("LAppDelegate: Touch moved to ($x, $y)")
        mouseX = x
        mouseY = y
        
        if (isCaptured) {
            view?.onTouchesMoved(mouseX, mouseY)
        }
    }

    fun deactivateApp() {
        isActive = false
    }

    // Getters
    fun getContext(): Context? = context
    fun getTextureManager(): LAppTextureManager? = textureManager
    fun getView(): LAppView? = view
    fun getWindowWidth(): Int = windowWidth
    fun getWindowHeight(): Int = windowHeight
    fun getCurrentModel(): Int = currentModel
} 