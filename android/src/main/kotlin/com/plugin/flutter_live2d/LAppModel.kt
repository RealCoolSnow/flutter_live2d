package com.plugin.flutter_live2d

import com.live2d.sdk.cubism.framework.CubismDefaultParameterId.ParameterId
import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.CubismModelSettingJson
import com.live2d.sdk.cubism.framework.ICubismModelSetting
import com.live2d.sdk.cubism.framework.effect.CubismBreath
import com.live2d.sdk.cubism.framework.effect.CubismEyeBlink
import com.live2d.sdk.cubism.framework.id.CubismId
import com.live2d.sdk.cubism.framework.id.CubismIdManager
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.model.CubismMoc
import com.live2d.sdk.cubism.framework.model.CubismUserModel
import com.live2d.sdk.cubism.framework.motion.ACubismMotion
import com.live2d.sdk.cubism.framework.motion.CubismExpressionMotion
import com.live2d.sdk.cubism.framework.motion.CubismMotion
import com.live2d.sdk.cubism.framework.motion.IBeganMotionCallback
import com.live2d.sdk.cubism.framework.motion.IFinishedMotionCallback
import com.live2d.sdk.cubism.framework.rendering.android.CubismOffscreenSurfaceAndroid
import com.live2d.sdk.cubism.framework.rendering.android.CubismRendererAndroid
import java.util.*

class LAppModel : CubismUserModel() {
    private var modelSetting: ICubismModelSetting? = null
    private var modelHomeDirectory: String = ""
    private var userTimeSeconds: Float = 0.0f

    private val eyeBlinkIds = mutableListOf<CubismId>()
    private val lipSyncIds = mutableListOf<CubismId>()
    private val motions = mutableMapOf<String, ACubismMotion>()
    private val expressions = mutableMapOf<String, ACubismMotion>()

    private val idParamAngleX: CubismId
    private val idParamAngleY: CubismId
    private val idParamAngleZ: CubismId
    private val idParamBodyAngleX: CubismId
    private val idParamEyeBallX: CubismId
    private val idParamEyeBallY: CubismId

    private val renderingBuffer = CubismOffscreenSurfaceAndroid()

    init {
        if (LAppDefine.MOC_CONSISTENCY_VALIDATION_ENABLE) {
            mocConsistency = true
        }

        if (LAppDefine.DEBUG_LOG_ENABLE) {
            debugMode = true
        }

        val idManager = CubismFramework.getIdManager()
        idParamAngleX = idManager.getId(ParameterId.ANGLE_X.id)
        idParamAngleY = idManager.getId(ParameterId.ANGLE_Y.id)
        idParamAngleZ = idManager.getId(ParameterId.ANGLE_Z.id)
        idParamBodyAngleX = idManager.getId(ParameterId.BODY_ANGLE_X.id)
        idParamEyeBallX = idManager.getId(ParameterId.EYE_BALL_X.id)
        idParamEyeBallY = idManager.getId(ParameterId.EYE_BALL_Y.id)
    }

    fun loadAssets(dir: String, fileName: String) {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("load model setting: $fileName")
        }

        // Ensure the directory has flutter_assets prefix
        modelHomeDirectory = LAppDefine.PathUtils.ensureFlutterAssetsPath(dir)
        val filePath = modelHomeDirectory + fileName

        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("Model file path: $filePath")
        }

        // 读取JSON
        val buffer = createBuffer(filePath)
        val setting = CubismModelSettingJson(buffer)

        // 设置模型
        setupModel(setting)

        if (model == null) {
            LAppPal.printLog("Failed to loadAssets().")
            return
        }

        // 设置渲染器
        val renderer = CubismRendererAndroid.create()
        setupRenderer(renderer)

        setupTextures()
    }

    fun deleteModel() {
        delete()
    }

    fun update() {
        val deltaTimeSeconds = LAppPal.getDeltaTime()
        userTimeSeconds += deltaTimeSeconds

        dragManager.update(deltaTimeSeconds)
        val dragX = dragManager.x
        val dragY = dragManager.y

        // 检查动作更新状态
        var isMotionUpdated = false

        // 加载上次保存的状态
        model?.loadParameters()

        // 如果没有动作播放，从待机动作中随机选择一个播放
        if (motionManager.isFinished()) {
            startRandomMotion(LAppDefine.MotionGroup.IDLE.id, LAppDefine.Priority.IDLE.priority)
        } else {
            // 更新动作
            isMotionUpdated = motionManager.updateMotion(model, deltaTimeSeconds)
        }

        // 保存模型状态
        model?.saveParameters()

        // 不透明度
        opacity = model?.modelOpacity ?: 0.0f

        // 眨眼
        if (!isMotionUpdated) {
            eyeBlink?.updateParameters(model, deltaTimeSeconds)
        }

        // 表情
        expressionManager?.updateMotion(model, deltaTimeSeconds)

        // 拖拽跟随
        model?.apply {
            // 调整脸部朝向
            addParameterValue(idParamAngleX, dragX * 30)
            addParameterValue(idParamAngleY, dragY * 30)
            addParameterValue(idParamAngleZ, dragX * dragY * -30)

            // 调整身体朝向
            addParameterValue(idParamBodyAngleX, dragX * 10)

            // 调整眼球方向
            addParameterValue(idParamEyeBallX, dragX)
            addParameterValue(idParamEyeBallY, dragY)
        }

        // 呼吸效果
        breath?.updateParameters(model, deltaTimeSeconds)

        // 物理效果
        physics?.evaluate(model, deltaTimeSeconds)

        // 口型同步
        if (lipSync) {
            val value = 0.0f
            lipSyncIds.forEach { lipSyncId ->
                model?.addParameterValue(lipSyncId, value, 0.8f)
            }
        }

        // 姿势更新
        pose?.updateParameters(model, deltaTimeSeconds)

        model?.update()
    }

    fun startMotion(
        group: String,
        number: Int,
        priority: Int,
        onFinishedMotionHandler: IFinishedMotionCallback? = null,
        onBeganMotionHandler: IBeganMotionCallback? = null
    ): Int {
        if (priority == LAppDefine.Priority.FORCE.priority) {
            motionManager.setReservationPriority(priority)
        } else if (!motionManager.reserveMotion(priority)) {
            if (debugMode) {
                LAppPal.printLog("Cannot start motion.")
            }
            return -1
        }

        val name = "${group}_$number"
        var motion = motions[name] as? CubismMotion

        if (motion == null) {
            val fileName = modelSetting?.getMotionFileName(group, number)
            if (!fileName.isNullOrEmpty()) {
                val path = modelHomeDirectory + fileName
                val buffer = createBuffer(path)

                motion = loadMotion(buffer, onFinishedMotionHandler, onBeganMotionHandler)
                motion?.apply {
                    modelSetting?.getMotionFadeInTimeValue(group, number)?.let { 
                        if (it != -1.0f) setFadeInTime(it) 
                    }
                    modelSetting?.getMotionFadeOutTimeValue(group, number)?.let { 
                        if (it != -1.0f) setFadeOutTime(it) 
                    }
                    setEffectIds(eyeBlinkIds, lipSyncIds)
                }
            }
        } else {
            motion.setFinishedMotionHandler(onFinishedMotionHandler)
            motion.setBeganMotionHandler(onBeganMotionHandler)
        }

        // 加载音频文件
        val voice = modelSetting?.getMotionSoundFileName(group, number)
        if (!voice.isNullOrEmpty()) {
            val path = modelHomeDirectory + voice
            LAppWavFileHandler(path).start()
        }

        if (debugMode) {
            LAppPal.printLog("start motion: ${group}_$number")
        }

        return motionManager.startMotionPriority(motion, priority)
    }

    fun startRandomMotion(
        group: String,
        priority: Int,
        onFinishedMotionHandler: IFinishedMotionCallback? = null,
        onBeganMotionHandler: IBeganMotionCallback? = null
    ): Int {
        modelSetting?.let {
            if (it.getMotionCount(group) == 0) {
                return -1
            }

            val number = Random().nextInt(it.getMotionCount(group))
            return startMotion(group, number, priority, onFinishedMotionHandler, onBeganMotionHandler)
        }
        return -1
    }

    fun draw(matrix: CubismMatrix44) {
        if (model == null) return

        CubismMatrix44.multiply(
            modelMatrix.array,
            matrix.array,
            matrix.array
        )

        (getRenderer() as? CubismRendererAndroid)?.apply {
            setMvpMatrix(matrix)
            drawModel()
        }
    }

    fun hitTest(hitAreaName: String, x: Float, y: Float): Boolean {
        // 透明时无碰撞检测
        if (opacity < 1) return false

        modelSetting?.let {
            for (i in 0 until it.hitAreasCount) {
                if (it.getHitAreaName(i) == hitAreaName) {
                    return isHit(it.getHitAreaId(i), x, y)
                }
            }
        }
        return false
    }

    fun setExpression(expressionId: String) {
        val motion = expressions[expressionId]

        if (debugMode) {
            LAppPal.printLog("expression: $expressionId")
        }

        if (motion != null) {
            expressionManager.startMotionPriority(motion, LAppDefine.Priority.FORCE.priority)
        } else {
            if (debugMode) {
                LAppPal.printLog("expression $expressionId is null")
            }
        }
    }

    fun setRandomExpression() {
        if (expressions.isEmpty()) return

        val number = Random().nextInt(expressions.size)
        expressions.keys.elementAtOrNull(number)?.let { setExpression(it) }
    }

    fun getRenderingBuffer(): CubismOffscreenSurfaceAndroid = renderingBuffer

    private fun setupModel(setting: ICubismModelSetting) {
        modelSetting = setting

        // 加载Cubism模型
        setting.modelFileName.takeIf { it.isNotEmpty() }?.let { fileName ->
            val path = modelHomeDirectory + fileName
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("create model: $fileName")
            }
            loadModel(createBuffer(path), mocConsistency)
        }

        // 加载表情文件
        if (setting.expressionCount > 0) {
            for (i in 0 until setting.expressionCount) {
                setting.getExpressionFileName(i).takeIf { it.isNotEmpty() }?.let { path ->
                    val buffer = createBuffer(modelHomeDirectory + path)
                    loadExpression(buffer)?.let { motion ->
                        expressions[setting.getExpressionName(i)] = motion
                    }
                }
            }
        }

        // 加载物理效果
        setting.physicsFileName.takeIf { it.isNotEmpty() }?.let { path ->
            loadPhysics(createBuffer(modelHomeDirectory + path))
        }

        // 加载姿势数据
        setting.poseFileName.takeIf { it.isNotEmpty() }?.let { path ->
            loadPose(createBuffer(modelHomeDirectory + path))
        }

        // 设置眨眼
        if (setting.eyeBlinkParameterCount > 0) {
            eyeBlink = CubismEyeBlink.create(setting)
        }

        // 设置呼吸效果
        breath = CubismBreath.create().apply {
            parameters = listOf(
                CubismBreath.BreathParameterData(idParamAngleX, 0.0f, 15.0f, 6.5345f, 0.5f),
                CubismBreath.BreathParameterData(idParamAngleY, 0.0f, 8.0f, 3.5345f, 0.5f),
                CubismBreath.BreathParameterData(idParamAngleZ, 0.0f, 10.0f, 5.5345f, 0.5f),
                CubismBreath.BreathParameterData(idParamBodyAngleX, 0.0f, 4.0f, 15.5345f, 0.5f),
                CubismBreath.BreathParameterData(
                    CubismFramework.getIdManager().getId(ParameterId.BREATH.id),
                    0.5f, 0.5f, 3.2345f, 0.5f
                )
            )
        }

        // 加载用户数据
        setting.userDataFile.takeIf { it.isNotEmpty() }?.let { path ->
            loadUserData(createBuffer(modelHomeDirectory + path))
        }

        // 设置眨眼参数ID
        for (i in 0 until setting.eyeBlinkParameterCount) {
            eyeBlinkIds.add(setting.getEyeBlinkParameterId(i))
        }

        // 设置口型同步参数ID
        for (i in 0 until setting.lipSyncParameterCount) {
            lipSyncIds.add(setting.getLipSyncParameterId(i))
        }

        // 设置布局
        val layout = mutableMapOf<String, Float>()
        if (setting.getLayoutMap(layout)) {
            modelMatrix.setupFromLayout(layout)
        }

        model?.saveParameters()

        // 预加载所有动作组
        for (i in 0 until setting.motionGroupCount) {
            preLoadMotionGroup(setting.getMotionGroupName(i))
        }

        motionManager.stopAllMotions()
    }

    private fun preLoadMotionGroup(group: String) {
        modelSetting?.let { setting ->
            for (i in 0 until setting.getMotionCount(group)) {
                val name = "${group}_$i"
                setting.getMotionFileName(group, i).takeIf { it.isNotEmpty() }?.let { path ->
                    val modelPath = LAppDefine.PathUtils.getFullResourcePath(modelHomeDirectory, path)
                    if (debugMode) {
                        LAppPal.printLog("load motion: $path===>[$name]")
                    }

                    loadMotion(createBuffer(modelPath))?.apply {
                        setting.getMotionFadeInTimeValue(group, i).takeIf { it != -1.0f }?.let { 
                            setFadeInTime(it) 
                        }
                        setting.getMotionFadeOutTimeValue(group, i).takeIf { it != -1.0f }?.let { 
                            setFadeOutTime(it) 
                        }
                        setEffectIds(eyeBlinkIds, lipSyncIds)
                        motions[name] = this
                    }
                }
            }
        }
    }

    private fun setupTextures() {
        val textureManager = LAppDelegate.getInstance().getTextureManager()
            ?: run {
                if (LAppDefine.DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("TextureManager is null")
                }
                return
            }

        modelSetting?.let { setting ->
            for (modelTextureNumber in 0 until setting.textureCount) {
                setting.getTextureFileName(modelTextureNumber).takeIf { it.isNotEmpty() }?.let { texturePath ->
                    // The path is already relative to modelHomeDirectory which has flutter_assets prefix
                    val path = modelHomeDirectory + texturePath
                    if (LAppDefine.DEBUG_LOG_ENABLE) {
                        LAppPal.printLog("Loading texture: $path")
                    }
                    try {
                        val texture = textureManager.createTextureFromPngFile(path)

                        (getRenderer() as? CubismRendererAndroid)?.apply {
                            bindTexture(modelTextureNumber, texture.id)
                            isPremultipliedAlpha(LAppDefine.PREMULTIPLIED_ALPHA_ENABLE)
                        }
                    } catch (e: Exception) {
                        if (LAppDefine.DEBUG_LOG_ENABLE) {
                            LAppPal.printLog("Failed to load texture: $path")
                            e.printStackTrace()
                        }
                        throw e  // Re-throw the exception after logging
                    }
                }
            }
        }
    }

    companion object {
        private fun createBuffer(path: String): ByteArray {
            val fullPath = LAppDefine.PathUtils.ensureFlutterAssetsPath(path)
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("create buffer: $fullPath")
            }
            return LAppPal.loadFileAsBytes(fullPath)
        }
    }

    /**
     * 设置模型的缩放比例
     */
    fun setScale(scale: Float) {
        if (model == null) return
        
        // 获取当前模型矩阵
        val modelMatrix = getModelMatrix()
        
        // 重置矩阵
        modelMatrix.loadIdentity()
        
        // 应用缩放
        modelMatrix.scale(scale, scale)
    }

    /**
     * 设置模型的位置
     */
    fun setPosition(x: Float, y: Float) {
        if (model == null) return
        
        val modelMatrix = getModelMatrix()
        
        // 设置位置
        modelMatrix.translate(x, y)
    }

    /**
     * 检查当前动作是否已完成
     */
    fun isMotionFinished(): Boolean {
        return motionManager.isFinished()
    }
} 