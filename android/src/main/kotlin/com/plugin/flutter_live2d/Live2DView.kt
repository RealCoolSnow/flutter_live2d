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

        // OpenGL ES 2.0设置必须在setRenderer之前
        setEGLContextClientVersion(2)
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Initialize OpenGL with transparent background
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        
        // 创建renderer
        renderer = CubismRendererAndroid.create() as CubismRendererAndroid
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        
        // Initialize model matrix
        modelMatrix = CubismMatrix44.create()
        modelMatrix?.loadIdentity()
        
        // Scale model to fit screen
        val modelScale = height / 2.0f
        modelMatrix?.scale(modelScale, modelScale)
        
        // Center model
        modelMatrix?.translateX(width / 2.0f)
        modelMatrix?.translateY(height / 2.0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Clear buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        model?.let { model ->
            // Update model matrix
            modelMatrix?.let { matrix ->
                matrix.loadIdentity()
                matrix.scale(scale, scale)
                matrix.translate(positionX, positionY)
                
                // Update and draw model
                model.update()
                model.draw(matrix)
            }
        }
    }

    fun loadModel(modelPath: String) {
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