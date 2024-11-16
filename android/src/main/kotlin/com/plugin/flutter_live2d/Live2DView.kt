package com.plugin.flutter_live2d

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.motion.CubismMotionQueueManager
import com.live2d.sdk.cubism.framework.model.CubismUserModel
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.rendering.android.CubismOffscreenSurfaceAndroid
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20
import com.live2d.sdk.cubism.core.ICubismLogger
import com.live2d.sdk.cubism.framework.CubismFrameworkConfig.LogLevel
import com.live2d.sdk.cubism.framework.rendering.android.CubismRendererAndroid

class Live2DView(context: Context) : GLSurfaceView(context), GLSurfaceView.Renderer {
    private var model: Live2DModel? = null
    private var renderer: CubismRendererAndroid? = null
    private var modelMatrix: CubismMatrix44? = null
    
    private var scale = 1.0f
    private var positionX = 0.0f
    private var positionY = 0.0f

    init {
        println("Live2DView: Initializing...")
        // 设置透明背景
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        
        // Initialize Cubism Framework
        val option = CubismFramework.Option()
        option.logFunction = object : ICubismLogger {
            override fun print(message: String) {
                println("Live2D: $message")
            }
        }
        option.loggingLevel = LogLevel.VERBOSE
        
        CubismFramework.startUp(option)
        CubismFramework.initialize()

        setEGLContextClientVersion(2)
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        println("Live2DView: Surface created")
        // Set texture sampling
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)

        // Set transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Initialize Cubism SDK framework
        CubismFramework.initialize()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        println("Live2DView: Surface changed: $width x $height")
        // Set drawing range
        GLES20.glViewport(0, 0, width, height)

        // Initialize model matrix
        modelMatrix = CubismMatrix44.create()
        modelMatrix?.loadIdentity()

        // Scale model to fit screen
        if (model?.getModel()?.getCanvasWidth() ?: 0f > 1.0f && width < height) {
            // For horizontal models in vertical windows, calculate scale from model width
            modelMatrix?.scale(2.0f, 2.0f)
            modelMatrix?.scale(1.0f, width.toFloat() / height.toFloat())
        } else {
            modelMatrix?.scale(height.toFloat() / width.toFloat(), 1.0f)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        // Clear screen
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glClearDepthf(1.0f)

        model?.let { model ->
            println("Live2DView: Drawing frame")
            // Update model matrix
            modelMatrix?.let { matrix ->
                matrix.loadIdentity()
                matrix.scale(scale, scale)
                matrix.translate(positionX, positionY)
                
                // Update and draw model
                model.update()
                model.draw(matrix)
            }
        } ?: run {
            println("Live2DView: No model to draw")
        }
    }

    fun loadModel(modelPath: String) {
        println("Live2DView: Loading model: $modelPath")
        val model = Live2DModel(context)
        // Extract directory and filename from modelPath
        val lastSlash = modelPath.lastIndexOf('/')
        val dir = modelPath.substring(0, lastSlash + 1)
        val fileName = modelPath.substring(lastSlash + 1)
        
        model.loadAssets(dir, fileName)
        this.model = model
    }

    fun startMotion(group: String, index: Int, priority: Int = 0) {
        model?.startMotion(group, index, priority)
    }

    fun setExpression(expressionId: String) {
        model?.setExpression(expressionId)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                positionX = event.x 
                positionY = event.y
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setScale(scale: Float) {
        this.scale = scale
    }

    fun setPosition(x: Float, y: Float) {
        this.positionX = x
        this.positionY = y
    }

    // Add this public method for cleanup
    fun cleanup() {
        onDetachedFromWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Release resources
        renderer?.close()
        CubismFramework.dispose()
    }
} 