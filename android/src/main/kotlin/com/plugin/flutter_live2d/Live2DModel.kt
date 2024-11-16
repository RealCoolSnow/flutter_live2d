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

class Live2DModel(private val context: Context) : CubismUserModel() {
    private var modelSetting: ICubismModelSetting? = null
    private var modelHomeDirectory: String = ""
    private var userTimeSeconds: Float = 0.0f
    
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
        
        // Setup model matrix
        val modelMatrix = CubismModelMatrix.create(model.getCanvasWidth(), model.getCanvasHeight())
        this.modelMatrix = modelMatrix
        
        // Load motions
        modelSetting?.let { setting ->
            for (i in 0 until setting.motionGroupCount) {
                val group = setting.getMotionGroupName(i)
                preLoadMotionGroup(group)
            }
        }
    }

    fun startMotion(group: String, no: Int, priority: Int) {
        modelSetting?.let { setting ->
            val fileName = setting.getMotionFileName(group, no)
            if (fileName.isNotEmpty()) {
                val path = modelHomeDirectory + fileName
                val buffer = createBuffer(path)
                val motion = loadMotion(buffer)
                motion?.let {
                    val fadeInTime = setting.getMotionFadeInTimeValue(group, no)
                    val fadeOutTime = setting.getMotionFadeOutTimeValue(group, no)
                    
                    if (fadeInTime >= 0.0f) {
                        it.setFadeInTime(fadeInTime)
                    }
                    if (fadeOutTime >= 0.0f) {
                        it.setFadeOutTime(fadeOutTime)
                    }
                    
                    motionManager.startMotionPriority(it, priority)
                }
            }
        }
    }

    fun setExpression(expressionId: String) {
        modelSetting?.let { setting ->
            for (i in 0 until setting.expressionCount) {
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
            context.assets.list("")?.forEach { 
                println("Available asset: $it")
            }
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

    private fun preLoadMotionGroup(group: String) {
        modelSetting?.let { setting ->
            for (i in 0 until setting.getMotionCount(group)) {
                val fileName = setting.getMotionFileName(group, i)
                if (fileName.isNotEmpty()) {
                    val path = modelHomeDirectory + fileName
                    val buffer = createBuffer(path)
                    loadMotion(buffer)
                }
            }
        }
    }

    fun update() {
        val deltaTimeSeconds = 1.0f / 60.0f // Or implement proper time delta
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

        val renderer = getRenderer() as CubismRendererAndroid
        renderer.setMvpMatrix(matrix)
        renderer.drawModel()
    }
} 