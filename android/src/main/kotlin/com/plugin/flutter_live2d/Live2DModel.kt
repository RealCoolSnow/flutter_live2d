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

class Live2DModel(private val context: Context) : CubismUserModel() {
    private var modelSetting: ICubismModelSetting? = null
    private var modelHomeDirectory: String = ""
    private var userTimeSeconds: Float = 0.0f
    private var modelMatrix: CubismModelMatrix? = null
    private var scale = 1.0f
    private var positionX = 0.0f
    private var positionY = 0.0f

    fun loadAssets(dir: String, fileName: String) {
        println("Live2DModel: Loading assets from dir: $dir, file: $fileName")
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
            
            // Initialize model matrix after loading model
            model?.let { model ->
                modelMatrix = CubismModelMatrix.create(
                    model.getCanvasWidth(),
                    model.getCanvasHeight()
                )
            }
        }
        
        // Setup renderer
        val renderer = CubismRendererAndroid.create()
        setupRenderer(renderer)
        
        // Setup eye blink
        if (modelSetting?.eyeBlinkParameterCount ?: 0 > 0) {
            eyeBlink = CubismEyeBlink.create(modelSetting)
        }
        
        // Setup breath
        breath = CubismBreath.create()
        val breathParameters = ArrayList<CubismBreath.BreathParameterData>()
        breathParameters.add(CubismBreath.BreathParameterData(CubismFramework.getIdManager().getId("ParamAngleX"), 0.0f, 15.0f, 6.5345f, 0.5f))
        breathParameters.add(CubismBreath.BreathParameterData(CubismFramework.getIdManager().getId("ParamAngleY"), 0.0f, 8.0f, 3.5345f, 0.5f))
        breathParameters.add(CubismBreath.BreathParameterData(CubismFramework.getIdManager().getId("ParamAngleZ"), 0.0f, 10.0f, 5.5345f, 0.5f))
        breathParameters.add(CubismBreath.BreathParameterData(CubismFramework.getIdManager().getId("ParamBodyAngleX"), 0.0f, 4.0f, 15.5345f, 0.5f))
        breath?.setParameters(breathParameters)
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
        if (model == null) {
            return
        }

        // Update model matrix
        modelMatrix?.let { modelMatrix ->
            modelMatrix.loadIdentity()
            modelMatrix.scale(scale, scale)
            modelMatrix.translate(positionX, positionY)
            
            // Multiply matrices
            CubismMatrix44.multiply(modelMatrix.getArray(), matrix.getArray(), matrix.getArray())
        }

        val renderer = getRenderer() as CubismRendererAndroid
        renderer.setMvpMatrix(matrix)
        renderer.drawModel()
    }

    fun setScale(scale: Float) {
        this.scale = scale
    }

    fun setPosition(x: Float, y: Float) {
        this.positionX = x
        this.positionY = y
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
            println("Live2DModel: Loading file: $assetPath")
            context.assets.open(assetPath).use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Live2DModel: Failed to load file: $path")
            println("Live2DModel: Error: ${e.message}")
            ByteArray(0)
        }
    }

    override fun getModelMatrix(): CubismModelMatrix? {
        return modelMatrix
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
} 