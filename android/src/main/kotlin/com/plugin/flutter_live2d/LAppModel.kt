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
    private var modelSetting: ICubismModelSetting? = null
    private var modelHomeDirectory: String = ""
    private var userTimeSeconds: Float = 0.0f
    private var modelScale = 1.0f
    private var modelPositionX = 0.0f
    private var modelPositionY = 0.0f
    private val userModelMatrix = CubismMatrix44.create()
    private lateinit var LAppTextureManager: LAppTextureManager
    private var shader: LAppSpriteShader? = null

    init {
        LAppTextureManager = LAppTextureManager(context)
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

                // 加载理 - 添加 flutter_assets 前缀
                val fullPath = "flutter_assets/$modelHomeDirectory$texturePath"
                println("LAppModel: Loading texture: $fullPath")
                
                val textureInfo = LAppTextureManager.createTextureFromPngFile(fullPath)
                
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
        val deltaTimeSeconds = 1.0f / 60.0f
        userTimeSeconds += deltaTimeSeconds

        // Update model parameters
        model?.loadParameters()
        
        // Update motion if exists
        motionManager.updateMotion(model, deltaTimeSeconds)
        
        // Update model
        model?.saveParameters()

        // Eye blink
        eyeBlink?.updateParameters(model, deltaTimeSeconds)
        
        // Breath
        breath?.updateParameters(model, deltaTimeSeconds)

        // Physics
        physics?.evaluate(model, deltaTimeSeconds)

        // Update pose
        pose?.updateParameters(model, deltaTimeSeconds)

        // Update model
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
                    
                    // 设置纹理
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glUniform1i(shader.getTextureLocation(), 0)
                    
                    // 设置基础颜色（包括透明度）
                    GLES20.glUniform4f(shader.getBaseColorLocation(), 1.0f, 1.0f, 1.0f, getOpacity())
                    
                    // 合并用户矩阵和投影矩阵
                    val drawMatrix = CubismMatrix44.create()
                    CubismMatrix44.multiply(userModelMatrix.getArray(), matrix.getArray(), drawMatrix.getArray())
                    
                    // 设置MVP矩阵
                    renderer.setMvpMatrix(drawMatrix)
                    
                    // 启用顶点属性
                    GLES20.glEnableVertexAttribArray(shader.getPositionLocation())
                    GLES20.glEnableVertexAttribArray(shader.getUvLocation())
                    
                    // 绘制模型
                    renderer.drawModel()
                    
                    // 禁用顶点属性
                    GLES20.glDisableVertexAttribArray(shader.getPositionLocation())
                    GLES20.glDisableVertexAttribArray(shader.getUvLocation())
                }
            }
        } catch (e: Exception) {
            println("LAppModel: Error drawing model: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun getOpacity(): Float {
        return model?.getModelOpacity() ?: 1.0f
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

    fun startMotion(group: String, index: Int, priority: Int = 0) {
        modelSetting?.let { setting ->
            val fileName = setting.getMotionFileName(group, index)
            if (fileName.isNotEmpty()) {
                val path = modelHomeDirectory + fileName
                val buffer = createBuffer(path)
                val motion = loadMotion(buffer)
                motion?.let {
                    motionManager.startMotionPriority(it, priority)
                }
            }
        }
    }

    fun setExpression(expressionId: String) {
        modelSetting?.let { setting ->
            for (i in 0 until setting.getExpressionCount()) {
                if (setting.getExpressionName(i) == expressionId) {
                    val fileName = setting.getExpressionFileName(i)
                    if (fileName.isNotEmpty()) {
                        val path = modelHomeDirectory + fileName
                        val buffer = createBuffer(path)
                        val motion = loadExpression(buffer)
                        motion?.let {
                            expressionManager.startMotionPriority(it, 0)
                        }
                    }
                    break
                }
            }
        }
    }

    private fun createBuffer(path: String): ByteArray {
        return try {
            val assetPath = "flutter_assets/$path"
            println("LAppModel: Loading file: $assetPath")
            
            // 列出可用的资源文件
            context.assets.list("")?.forEach { 
                println("Available asset root: $it")
            }
            context.assets.list("flutter_assets")?.forEach { 
                println("Available asset in flutter_assets: $it")
            }
            
            // 读取文件
            context.assets.open(assetPath).use { inputStream ->
                inputStream.readBytes().also { bytes ->
                    println("LAppModel: Successfully loaded ${bytes.size} bytes from $assetPath")
                }
            }
        } catch (e: Exception) {
            println("LAppModel: Failed to load file: $path")
            e.printStackTrace()
            ByteArray(0)
        }
    }

    override fun getModelMatrix(): CubismModelMatrix {
        return getModelMatrix()
    }

    override fun setDragging(x: Float, y: Float) {
        model?.let { model ->
            val dragX = x * 30.0f
            val dragY = y * 30.0f
            
            // Update parameters
            model.addParameterValue(CubismFramework.getIdManager().getId("ParamAngleX"), dragX)
            model.addParameterValue(CubismFramework.getIdManager().getId("ParamAngleY"), dragY)
            model.addParameterValue(CubismFramework.getIdManager().getId("ParamAngleZ"), dragX * dragY * (-30.0f))
            model.addParameterValue(CubismFramework.getIdManager().getId("ParamBodyAngleX"), dragX * 10.0f)
            model.addParameterValue(CubismFramework.getIdManager().getId("ParamEyeBallX"), dragX)
            model.addParameterValue(CubismFramework.getIdManager().getId("ParamEyeBallY"), dragY)
        }
    }

    fun hitTest(hitAreaName: String, x: Float, y: Float): Boolean {
        if (modelSetting == null) return false
        
        for (i in 0 until modelSetting!!.getHitAreasCount()) {
            if (modelSetting!!.getHitAreaName(i) == hitAreaName) {
                val drawId = modelSetting!!.getHitAreaId(i)
                return isHit(drawId, x, y)
            }
        }
        return false
    }

    override fun isHit(drawableId: CubismId, pointX: Float, pointY: Float): Boolean {
        val drawableIndex = model?.getDrawableIndex(drawableId) ?: -1
        if (drawableIndex < 0) return false

        // 获取可绘制对象的顶点位置
        val vertices = model?.getDrawableVertices(drawableIndex)
        if (vertices == null || vertices.isEmpty()) return false

        // 计算包围盒
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        for (i in vertices.indices step 2) {
            val x = vertices[i]
            val y = vertices[i + 1]
            minX = minOf(minX, x)
            minY = minOf(minY, y)
            maxX = maxOf(maxX, x)
            maxY = maxOf(maxY, y)
        }

        // 检查点是否在包围盒内
        return pointX >= minX && pointX <= maxX && pointY >= minY && pointY <= maxY
    }

    fun setRandomExpression() {
        if (modelSetting == null) return
        
        val expressionCount = modelSetting!!.getExpressionCount()
        if (expressionCount > 0) {
            val expressionIndex = (Math.random() * expressionCount).toInt()
            setExpression(modelSetting!!.getExpressionName(expressionIndex))
        }
    }

    fun startRandomMotion(group: String, priority: Int) {
        if (modelSetting == null) return
        
        val motionCount = modelSetting!!.getMotionCount(group)
        if (motionCount > 0) {
            val motionIndex = (Math.random() * motionCount).toInt()
            startMotion(group, motionIndex, priority)
        }
    }

    override fun setOpacity(opacity: Float) {
        model?.let { model ->
            // 设置模型的不透明度
            model.setParameterValue(
                CubismFramework.getIdManager().getId("ParamOpacity"),
                opacity
            )
            // 强制更新
            model.update()
        }
    }

    fun dispose() {
        println("LAppModel: Disposing...")
        try {
            // 放渲染器
            (getRenderer() as? CubismRendererAndroid)?.close()
            
            // 释放模型资源
            model?.close()
            
            // 清除引用
            modelSetting = null
            model = null
            
            // 删除着色器程序
            if (shader?.getShaderId() != 0) {
                GLES20.glDeleteProgram(shader?.getShaderId()!!)
            }
            
            shader?.dispose()
            
            println("LAppModel: Disposed successfully")
        } catch (e: Exception) {
            println("LAppModel: Error during disposal")
            e.printStackTrace()
        }
    }

    fun initializeShader() {
        // 在OpenGL上下文中创建着色器
        shader = LAppSpriteShader(context)
        if (shader?.getShaderId() == 0) {
            throw RuntimeException("Failed to create shader program")
        }
    }
} 