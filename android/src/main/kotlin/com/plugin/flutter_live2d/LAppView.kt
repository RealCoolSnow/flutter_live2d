package com.plugin.flutter_live2d

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.math.CubismViewMatrix
import com.live2d.sdk.cubism.framework.rendering.android.CubismOffscreenSurfaceAndroid

class LAppView(context: Context) : GLSurfaceView(context), AutoCloseable {
    /**
     * LAppModel的渲染目标
     */
    enum class RenderingTarget {
        NONE,                   // 默认帧缓冲
        MODEL_FRAME_BUFFER,     // 每个模型自己的帧缓冲
        VIEW_FRAME_BUFFER      // View持有的帧缓冲
    }

    private val deviceToScreen = CubismMatrix44.create() // 设备坐标到屏幕坐标的转换矩阵
    private val viewMatrix = CubismViewMatrix()         // 控制画面显示的缩放和移动的矩阵
    private var windowWidth: Int = 0
    private var windowHeight: Int = 0

    private var renderingTarget = RenderingTarget.NONE
    private val clearColor = FloatArray(4)
    private val renderingBuffer = CubismOffscreenSurfaceAndroid()

    private var backSprite: LAppSprite? = null
    // private var gearSprite: LAppSprite? = null
    // private var powerSprite: LAppSprite? = null
    private var renderingSprite: LAppSprite? = null

    private var isChangedModel = false
    private val touchManager = TouchManager()
    private lateinit var spriteShader: LAppSpriteShader

    init {
        clearColor[0] = 1.0f
        clearColor[1] = 1.0f
        clearColor[2] = 1.0f
        clearColor[3] = 0.0f
    }

    override fun close() {
        spriteShader.close()
    }

    /**
     * 初始化视图
     */
    fun initialize() {
        val width = LAppDelegate.getInstance().getWindowWidth()
        val height = LAppDelegate.getInstance().getWindowHeight()

        val ratio = width.toFloat() / height.toFloat()
        val left = -ratio
        val right = ratio
        val bottom = LAppDefine.LogicalView.LEFT.value
        val top = LAppDefine.LogicalView.RIGHT.value

        // 设置设备对应的屏幕范围
        viewMatrix.setScreenRect(left, right, bottom, top)
        viewMatrix.scale(LAppDefine.Scale.DEFAULT.value, LAppDefine.Scale.DEFAULT.value)

        // 初始化为单位矩阵
        deviceToScreen.loadIdentity()

        if (width > height) {
            val screenW = Math.abs(right - left)
            deviceToScreen.scaleRelative(screenW / width, -screenW / width)
        } else {
            val screenH = Math.abs(top - bottom)
            deviceToScreen.scaleRelative(screenH / height, -screenH / height)
        }
        deviceToScreen.translateRelative(-width * 0.5f, -height * 0.5f)

        // 设置显示范围
        viewMatrix.setMaxScale(LAppDefine.Scale.MAX.value)   // 最大缩放率
        viewMatrix.setMinScale(LAppDefine.Scale.MIN.value)   // 最小缩放率

        // 设置最大可显示范围
        viewMatrix.setMaxScreenRect(
            LAppDefine.MaxLogicalView.LEFT.value,
            LAppDefine.MaxLogicalView.RIGHT.value,
            LAppDefine.MaxLogicalView.BOTTOM.value,
            LAppDefine.MaxLogicalView.TOP.value
        )

        spriteShader = LAppSpriteShader()
    }

    /**
     * 初始化精灵
     */
    fun initializeSprite() {
        val windowWidth = LAppDelegate.getInstance().getWindowWidth()
        val windowHeight = LAppDelegate.getInstance().getWindowHeight()
        val textureManager = LAppDelegate.getInstance().getTextureManager()
            ?: run {
                if (LAppDefine.DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("TextureManager is null")
                }
                return
            }

        // 加载背景图像
        try {
            // val backgroundTexture = textureManager.createTextureFromPngFile(
            //     LAppDefine.ResourcePath.BACK_IMAGE.path
            // )

            // // x,y是图像的中心坐标
            // var x = windowWidth * 0.5f
            // var y = windowHeight * 0.5f
            // var fWidth = backgroundTexture.width * 2.0f
            // var fHeight = windowHeight * 0.95f

            // val programId = spriteShader.getShaderId()

            // if (backSprite == null) {
            //     backSprite = LAppSprite(x, y, fWidth, fHeight, backgroundTexture.id, programId)
            // } else {
            //     backSprite?.resize(x, y, fWidth, fHeight)
            // }

            // 加载齿轮图像
            // val gearTexture = textureManager.createTextureFromPngFile(
            //     LAppDefine.ResourcePath.GEAR_IMAGE.path
            // )

            // x = windowWidth - gearTexture.width * 0.5f - 96f
            // y = windowHeight - gearTexture.height * 0.5f
            // fWidth = gearTexture.width.toFloat()
            // fHeight = gearTexture.height.toFloat()

            // if (gearSprite == null) {
            //     gearSprite = LAppSprite(x, y, fWidth, fHeight, gearTexture.id, programId)
            // } else {
            //     gearSprite?.resize(x, y, fWidth, fHeight)
            // }

            // 加载电源图像
            // val powerTexture = textureManager.createTextureFromPngFile(
            //     LAppDefine.ResourcePath.POWER_IMAGE.path
            // )

            // x = windowWidth - powerTexture.width * 0.5f - 96.0f
            // y = powerTexture.height * 0.5f
            // fWidth = powerTexture.width.toFloat()
            // fHeight = powerTexture.height.toFloat()

            // if (powerSprite == null) {
            //     powerSprite = LAppSprite(x, y, fWidth, fHeight, powerTexture.id, programId)
            // } else {
            //     powerSprite?.resize(x, y, fWidth, fHeight)
            // }

            // 覆盖整个屏幕的尺寸
            x = windowWidth * 0.5f
            y = windowHeight * 0.5f

            if (renderingSprite == null) {
                renderingSprite = LAppSprite(x, y, windowWidth.toFloat(), windowHeight.toFloat(), 0, programId)
            } else {
                renderingSprite?.resize(x, y, windowWidth.toFloat(), windowHeight.toFloat())
            }
        } catch (e: Exception) {
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("Failed to initialize sprites: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * 渲染
     */
    fun render() {
        // 获取画面尺寸
        val maxWidth = LAppDelegate.getInstance().getWindowWidth()
        val maxHeight = LAppDelegate.getInstance().getWindowHeight()

        backSprite?.setWindowSize(maxWidth, maxHeight)
        // gearSprite?.setWindowSize(maxWidth, maxHeight)
        // powerSprite?.setWindowSize(maxWidth, maxHeight)

        // 渲染UI和背景
        backSprite?.render()
        // gearSprite?.render()
        // powerSprite?.render()

        if (isChangedModel) {
            isChangedModel = false
            LAppLive2DManager.getInstance().nextScene()
        }

        // 渲染模型
        val live2dManager = LAppLive2DManager.getInstance()
        live2dManager.onUpdate()

        // 如果使用模型自己的渲染目标作为纹理
        if (renderingTarget == RenderingTarget.MODEL_FRAME_BUFFER && renderingSprite != null) {
            val uvVertex = floatArrayOf(
                1.0f, 1.0f,
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f
            )

            for (i in 0 until live2dManager.getModelNum()) {
                val model = live2dManager.getModel(i)
                // 只获取一个模型的不透明度
                val alpha = if (i < 1) 1.0f else model?.getOpacity() ?: 0f

                renderingSprite?.setColor(1.0f, 1.0f, 1.0f, alpha)

                model?.let {
                    renderingSprite?.setWindowSize(maxWidth, maxHeight)
                    renderingSprite?.renderImmediate(it.getRenderingBuffer().colorBuffer[0], uvVertex)
                }
            }
        }
    }

    /**
     * 在绘制模型之前调用
     */
    fun preModelDraw(refModel: LAppModel) {
        // 使用的离屏渲染目标
        val useTarget = when (renderingTarget) {
            RenderingTarget.VIEW_FRAME_BUFFER -> renderingBuffer
            RenderingTarget.MODEL_FRAME_BUFFER -> refModel.getRenderingBuffer()
            else -> null
        }

        // 如果需要渲染到其他目标
        useTarget?.let { target ->
            // 如果渲染目标未创建，在这里创建
            if (!target.isValid) {
                val width = LAppDelegate.getInstance().getWindowWidth()
                val height = LAppDelegate.getInstance().getWindowHeight()
                target.createOffscreenSurface(width, height, null)
            }
            // 开始渲染
            target.beginDraw(null)
            target.clear(clearColor[0], clearColor[1], clearColor[2], clearColor[3])
        }
    }

    /**
     * 在绘制模型之后调用
     */
    fun postModelDraw(refModel: LAppModel) {
        // 使用的离屏渲染目标
        val useTarget = when (renderingTarget) {
            RenderingTarget.VIEW_FRAME_BUFFER -> renderingBuffer
            RenderingTarget.MODEL_FRAME_BUFFER -> refModel.getRenderingBuffer()
            else -> null
        }

        useTarget?.let { target ->
            // 结束渲染
            target.endDraw()

            // 如果使用LAppView的帧缓冲，这里进行精灵绘制
            if (renderingTarget == RenderingTarget.VIEW_FRAME_BUFFER && renderingSprite != null) {
                val uvVertex = floatArrayOf(
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f
                )
                renderingSprite?.setColor(1.0f, 1.0f, 1.0f, getSpriteAlpha(0))

                val maxWidth = LAppDelegate.getInstance().getWindowWidth()
                val maxHeight = LAppDelegate.getInstance().getWindowHeight()

                renderingSprite?.setWindowSize(maxWidth, maxHeight)
                renderingSprite?.renderImmediate(target.colorBuffer[0], uvVertex)
            }
        }
    }

    /**
     * 切换渲染目标
     */
    fun switchRenderingTarget(targetType: RenderingTarget) {
        renderingTarget = targetType
    }

    /**
     * 触摸开始时调用
     */
    fun onTouchesBegan(pointX: Float, pointY: Float) {
        touchManager.touchesBegan(pointX, pointY)
    }

    /**
     * 触摸移动时调用
     */
    fun onTouchesMoved(pointX: Float, pointY: Float) {
        val viewX = transformViewX(touchManager.getLastX())
        val viewY = transformViewY(touchManager.getLastY())

        touchManager.touchesMoved(pointX, pointY)

        LAppLive2DManager.getInstance().onDrag(viewX, viewY)
    }

    /**
     * 触摸结束时调用
     */
    fun onTouchesEnded(pointX: Float, pointY: Float) {
        // 触摸结束
        val live2DManager = LAppLive2DManager.getInstance()
        live2DManager.onDrag(0.0f, 0.0f)

        // 单击
        val x = deviceToScreen.transformX(touchManager.getLastX())
        val y = deviceToScreen.transformY(touchManager.getLastY())

        if (LAppDefine.DEBUG_TOUCH_LOG_ENABLE) {
            LAppPal.printLog("Touches ended x: $x, y: $y")
        }

        live2DManager.onTap(x, y)

        // 检查是否点击了齿轮按钮
        // if (gearSprite?.isHit(pointX, pointY) == true) {
        //     isChangedModel = true
        // }

        // 检查是否点击了电源按钮
        // if (powerSprite?.isHit(pointX, pointY) == true) {
        //     LAppDelegate.getInstance().deactivateApp()
        // }
    }

    /**
     * 将X坐标转换为View坐标
     */
    fun transformViewX(deviceX: Float): Float {
        val screenX = deviceToScreen.transformX(deviceX)
        return viewMatrix.invertTransformX(screenX)
    }

    /**
     * 将Y坐标转换为View坐标
     */
    fun transformViewY(deviceY: Float): Float {
        val screenY = deviceToScreen.transformY(deviceY)
        return viewMatrix.invertTransformY(screenY)
    }

    /**
     * 将X坐标转换为Screen坐标
     */
    fun transformScreenX(deviceX: Float): Float {
        return deviceToScreen.transformX(deviceX)
    }

    /**
     * 将Y坐标转换为Screen坐标
     */
    fun transformScreenY(deviceY: Float): Float {
        return deviceToScreen.transformY(deviceY)
    }

    /**
     * 设置渲染目标的清除颜色
     */
    fun setRenderingTargetClearColor(r: Float, g: Float, b: Float) {
        clearColor[0] = r
        clearColor[1] = g
        clearColor[2] = b
    }

    /**
     * 获取精灵的alpha值
     */
    fun getSpriteAlpha(assign: Int): Float {
        // 根据assign值设置适当的差异
        var alpha = 0.25f + assign * 0.5f

        // 限制alpha值范围
        alpha = alpha.coerceIn(0.1f, 1.0f)
        return alpha
    }

    /**
     * 获取当前渲染目标
     */
    fun getRenderingTarget(): RenderingTarget = renderingTarget

    /**
     * 设置背景图片
     */
    fun setBackgroundImage(imagePath: String) {
        val textureManager = LAppDelegate.getInstance().getTextureManager()
            ?: run {
                if (LAppDefine.DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("TextureManager is null")
                }
                return
            }

        try {
            val backgroundTexture = textureManager.createTextureFromPngFile(imagePath)
            
            val windowWidth = LAppDelegate.getInstance().getWindowWidth()
            val windowHeight = LAppDelegate.getInstance().getWindowHeight()
            
            // x,y是图像的中心坐标
            val x = windowWidth * 0.5f
            val y = windowHeight * 0.5f
            val fWidth = backgroundTexture.width * 2.0f
            val fHeight = windowHeight * 0.95f

            val programId = spriteShader.getShaderId()

            if (backSprite == null) {
                backSprite = LAppSprite(x, y, fWidth, fHeight, backgroundTexture.id, programId)
            } else {
                // 更新现有精灵的纹理ID和尺寸
                backSprite?.apply {
                    // 假设LAppSprite有更新纹理ID的方法，如果没有需要添加
                    updateTextureId(backgroundTexture.id)
                    resize(x, y, fWidth, fHeight)
                }
            }

            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("Background image set successfully: $imagePath")
            }
        } catch (e: Exception) {
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("Failed to set background image: ${e.message}")
                e.printStackTrace()
            }
        }
    }
} 