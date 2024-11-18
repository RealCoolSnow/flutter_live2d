package com.plugin.flutter_live2d

import android.content.Context
import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.CubismModelSettingJson
import com.live2d.sdk.cubism.framework.ICubismModelSetting
import com.live2d.sdk.cubism.framework.effect.CubismBreath
import com.live2d.sdk.cubism.framework.effect.CubismEyeBlink
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.math.CubismModelMatrix
import com.live2d.sdk.cubism.framework.model.CubismUserModel
import com.live2d.sdk.cubism.framework.rendering.android.CubismRendererAndroid
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.utils.CubismDebug
import com.live2d.sdk.cubism.framework.rendering.CubismRenderer
import android.opengl.GLES20

class LAppModel(private val context: Context) : CubismUserModel() {
    private var model: CubismModel? = null
    private var modelSetting: ICubismModelSetting? = null
    private var modelHomeDirectory: String = ""
    private var userTimeSeconds: Float = 0.0f
    private var modelScale = 1.0f
    private var modelPositionX = 0.0f
    private var modelPositionY = 0.0f
    private val userModelMatrix = CubismMatrix44.create()
    private var textureManager: LAppTextureManager = LAppTextureManager(context)
    private var shader: LAppSpriteShader? = null

    init {
        if (LAppDefine.MOC_CONSISTENCY_VALIDATION_ENABLE) {
            mocConsistency = true
        }

        CubismIdManager.registerIds()
    }

    fun loadAssets(dir: String, fileName: String) {
        println("LAppModel: Loading assets from dir: $dir, file: $fileName")
        modelHomeDirectory = dir
        val path = modelHomeDirectory + fileName
        
        // Load model setting
        val buffer = createBuffer(path)
        modelSetting = CubismModelSettingJson(buffer)
        
        // Load model
        val modelFileName = modelSetting?.modelFileName
        if (!modelFileName.isNullOrEmpty()) {
            val modelPath = modelHomeDirectory + modelFileName
            val modelBuffer = createBuffer(modelPath)
            loadModel(modelBuffer)
            println("LAppModel: Model loaded")

            // Initialize user matrix
            userModelMatrix.loadIdentity()
            println("LAppModel: Matrix initialized")
        }
        
        // Setup renderer
        setupRenderer(CubismRendererAndroid.create())
        println("LAppModel: Renderer setup")
        
        // Setup textures
        setupTextures()
    }

    private fun setupTextures() {
        println("LAppModel: Setting up textures")
        try {
            val renderer = getRenderer() as CubismRendererAndroid
            
            // 设置渲染器属性
            renderer.isPremultipliedAlpha(true)
            
            for (modelTextureNumber in 0 until (modelSetting?.getTextureCount() ?: 0)) {
                // 获取纹理路径
                val texturePath = modelSetting?.getTextureFileName(modelTextureNumber)
                if (texturePath.isNullOrEmpty()) {
                    println("LAppModel: Empty texture path for number $modelTextureNumber")
                    continue
                }

                // 加载纹理 - 添加 flutter_assets 前缀
                val fullPath = "flutter_assets/$modelHomeDirectory$texturePath"
                println("LAppModel: Loading texture: $fullPath")
                
                val textureInfo = textureManager.createTextureFromPngFile(fullPath)
                
                // 绑定纹理到渲染器
                renderer.bindTexture(modelTextureNumber, textureInfo.id)
                
                println("LAppModel: Texture $modelTextureNumber bound to GL texture ${textureInfo.id}")
            }
            
            println("LAppModel: All textures set up")
        } catch (e: Exception) {
            println("LAppModel: Error setting up textures")
            e.printStackTrace()
        }
    }

    fun update() {
        val deltaTimeSeconds = LAppPal.getDeltaTime()
        userTimeSeconds += deltaTimeSeconds

        // Update model parameters
        model?.loadParameters()
        
        // Update motion if exists
        motionManager.updateMotion(model, deltaTimeSeconds)
        
        // Update model
        model?.saveParameters()

        // Update physics
        physics?.evaluate(model, deltaTimeSeconds)

        // Update pose
        pose?.updateParameters(model, deltaTimeSeconds)

        model?.update()
    }

    fun draw(matrix: CubismMatrix44) {
        try {
            val renderer = getRenderer() as? CubismRendererAndroid
            if (renderer != null) {
                // 确保着色器已创建
                if (shader == null) {
                    initializeShader()
                }
                
                shader?.let { shader ->
                    // 使用着色器程序
                    GLES20.glUseProgram(shader.getShaderId())
                    
                    // 合并用户矩阵和投影矩阵
                    val drawMatrix = CubismMatrix44.create()
                    CubismMatrix44.multiply(userModelMatrix.getArray(), matrix.getArray(), drawMatrix.getArray())
                    
                    // 设置MVP矩阵
                    renderer.setMvpMatrix(drawMatrix)
                    
                    // 绘制模型
                    renderer.drawModel()
                }
            }
        } catch (e: Exception) {
            println("LAppModel: Error drawing model: ${e.message}")
            e.printStackTrace()
        }
    }

    fun setScale(scale: Float) {
        modelScale = scale
        updateModelMatrix()
    }

    fun setPosition(x: Float, y: Float) {
        modelPositionX = x
        modelPositionY = y
        updateModelMatrix()
    }

    private fun updateModelMatrix() {
        userModelMatrix.loadIdentity()
        userModelMatrix.scale(modelScale, modelScale)
        userModelMatrix.translate(modelPositionX, modelPositionY)
    }

    private fun createBuffer(path: String): ByteArray {
        println("LAppModel: Creating buffer for path: $path")
        return context.assets.open(path).use { it.readBytes() }
    }

    fun release() {
        println("LAppModel: Releasing resources")
        model?.delete()
        model = null
        modelSetting = null
        textureManager = LAppTextureManager(context)
        println("LAppModel: Resources released")
    }

    // 其他必要的方法...
} 