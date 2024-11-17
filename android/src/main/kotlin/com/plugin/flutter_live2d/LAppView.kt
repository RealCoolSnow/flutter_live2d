package com.plugin.flutter_live2d

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.math.CubismViewMatrix

class LAppView(context: Context) : GLSurfaceView(context) {
    // 添加渲染目标枚举
    enum class RenderingTarget {
        NONE,                   // 默认帧缓冲
        MODEL_FRAME_BUFFER,     // 每个模型自己的帧缓冲
        VIEW_FRAME_BUFFER      // View持有的帧缓冲
    }

    private var delegate: LAppDelegate = LAppDelegate.getInstance()
    private val renderer: GLRenderer
    private var shader: LAppSpriteShader? = null
    private var backSprite: LAppSprite? = null
    private var renderingSprite: LAppSprite? = null
    private var renderingTarget = RenderingTarget.NONE
    
    // 视图矩阵相关
    private val viewMatrix = CubismViewMatrix()
    private val deviceToScreen = CubismMatrix44.create()
    private var maxScale = 2.0f
    private var minScale = 0.8f

    private var backImagePath: String? = null
    private var textureManager = LAppTextureManager(context)

    init {
        println("LAppView: Initializing GLSurfaceView...")
        
        // 设置OpenGL ES 2.0
        setEGLContextClientVersion(2)
        
        // 设置EGL配置
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        
        // 创建渲染器
        renderer = GLRenderer()
        
        // 设置渲染器
        setRenderer(renderer)
        
        // 设置渲染模式为连续渲染
        renderMode = RENDERMODE_CONTINUOUSLY

        // 等待OpenGL上下文创建完成
        queueEvent {
            println("LAppView: OpenGL context created")
            // 初始化Live2D
            delegate.onStart(context)
            // 初始化着色器和精灵
            initialize()
        }
    }

    private fun initialize() {
        // 创建着色器
        shader = LAppSpriteShader(context)
        val programId = shader?.getShaderId() ?: return

        val width = delegate.getWindowWidth()
        val height = delegate.getWindowHeight()

        // 设置视图矩阵
        val ratio = width.toFloat() / height.toFloat()
        val left = -ratio
        val right = ratio
        val bottom = -1.0f
        val top = 1.0f

        viewMatrix.setScreenRect(left, right, bottom, top)
        viewMatrix.scale(1.0f, 1.0f)
        viewMatrix.setMaxScale(maxScale)
        viewMatrix.setMinScale(minScale)
        viewMatrix.setMaxScreenRect(-2.0f, 2.0f, -2.0f, 2.0f)

        // 设置设备到屏幕的转换矩阵
        deviceToScreen.loadIdentity()
        if (width > height) {
            val screenW = Math.abs(right - left)
            deviceToScreen.scaleRelative(screenW / width, -screenW / width)
        } else {
            val screenH = Math.abs(top - bottom)
            deviceToScreen.scaleRelative(screenH / height, -screenH / height)
        }
        deviceToScreen.translateRelative(-width * 0.5f, -height * 0.5f)

        // 创建渲染精灵
        renderingSprite = LAppSprite(
            width * 0.5f,
            height * 0.5f,
            width.toFloat(),
            height.toFloat(),
            0,
            programId
        ).apply {
            setWindowSize(width, height)
        }
    }

    fun setBackgroundImage(imagePath: String) {
        println("LAppView: Setting background image: $imagePath")
        backImagePath = imagePath
        queueEvent {
            println("LAppView: Creating background sprite in GL context")
            createBackSprite()
        }
    }

    private fun createBackSprite() {
        backImagePath?.let { path ->
            try {
                println("LAppView: Starting to create background sprite")
                
                // 创建着色器（如果还没有创建）
                if (shader == null) {
                    println("LAppView: Creating shader")
                    shader = LAppSpriteShader(context)
                }
                val programId = shader?.getShaderId() ?: run {
                    println("LAppView: Failed to get shader program ID")
                    return
                }
                println("LAppView: Got shader program ID: $programId")

                // 加载纹理
                val fullPath = "flutter_assets/$path"
                println("LAppView: Loading texture from path: $fullPath")
                val textureInfo = try {
                    textureManager.createTextureFromPngFile(fullPath)
                } catch (e: Exception) {
                    println("LAppView: Failed to load texture: ${e.message}")
                    e.printStackTrace()
                    return
                }
                println("LAppView: Texture loaded with ID: ${textureInfo.id}")

                // 获取窗口尺寸
                val width = delegate.getWindowWidth()
                val height = delegate.getWindowHeight()
                println("LAppView: Window size: $width x $height")

                // 计算背景图尺寸（保持纵横比，填满屏幕）
                val scaleX = width.toFloat() / textureInfo.width
                val scaleY = height.toFloat() / textureInfo.height
                val scale = maxOf(scaleX, scaleY)
                val scaledWidth = textureInfo.width * scale
                val scaledHeight = textureInfo.height * scale
                println("LAppView: Scaled dimensions: $scaledWidth x $scaledHeight")

                // 创建或更新背景精灵
                if (backSprite == null) {
                    println("LAppView: Creating new background sprite")
                    backSprite = LAppSprite(
                        width * 0.5f,
                        height * 0.5f,
                        scaledWidth,
                        scaledHeight,
                        textureInfo.id,
                        programId
                    )
                } else {
                    println("LAppView: Updating existing background sprite")
                    backSprite?.resize(
                        width * 0.5f,
                        height * 0.5f,
                        scaledWidth,
                        scaledHeight
                    )
                }
                backSprite?.setWindowSize(width, height)
                println("LAppView: Background sprite setup complete")
            } catch (e: Exception) {
                println("LAppView: Failed to create background sprite: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun render() {
        // 更新精灵窗口大小
        val width = delegate.getWindowWidth()
        val height = delegate.getWindowHeight()
        
        // 渲染背景
        if (backSprite != null) {
            println("LAppView: Rendering background sprite")
            backSprite?.setWindowSize(width, height)
            backSprite?.render()
        }

        // 渲染模型
        delegate.run()

        // 如果使用单独的渲染目标，渲染精灵
        if (renderingTarget != RenderingTarget.NONE && renderingSprite != null) {
            val model = delegate.getLAppLive2DManager()?.getModel()
            if (model != null) {
                // 设置精灵的颜色和透明度
                renderingSprite?.setColor(1.0f, 1.0f, 1.0f, 1.0f)
                
                // 使用着色器
                shader?.let { shader ->
                    GLES20.glUseProgram(shader.getShaderId())
                    
                    // 设置纹理
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glUniform1i(shader.getTextureLocation(), 0)
                }
                
                // 渲染精灵
                renderingSprite?.render()
            }
        }
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

    fun transformViewX(deviceX: Float): Float {
        val screenX = deviceToScreen.transformX(deviceX)
        return viewMatrix.invertTransformX(screenX)
    }

    fun transformViewY(deviceY: Float): Float {
        val screenY = deviceToScreen.transformY(deviceY)
        return viewMatrix.invertTransformY(screenY)
    }

    fun dispose() {
        println("LAppView: Disposing...")
        delegate.onStop()
        shader?.dispose()
    }

    // 添加切换渲染目标的方法
    fun switchRenderingTarget(target: RenderingTarget) {
        renderingTarget = target
    }
} 