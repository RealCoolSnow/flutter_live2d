package com.plugin.flutter_live2d

import com.live2d.sdk.cubism.framework.math.CubismMatrix44

class LAppLive2DManager private constructor() {
    companion object {
        private var instance: LAppLive2DManager? = null

        fun getInstance(): LAppLive2DManager {
            if (instance == null) {
                instance = LAppLive2DManager()
            }
            return instance!!
        }

        fun releaseInstance() {
            instance = null
        }
    }

    private val models = mutableListOf<LAppModel>()
    private var currentModel: Int = 0
    private val viewMatrix = CubismMatrix44.create()
    private val projection = CubismMatrix44.create()

    /**
     * 释放所有模型
     */
    fun releaseAllModel() {
        for (model in models) {
            model.deleteModel()
        }
        models.clear()
    }

    /**
     * 更新模型
     */
    fun onUpdate() {
        val width = LAppDelegate.getInstance().getWindowWidth()
        val height = LAppDelegate.getInstance().getWindowHeight()

        for (model in models) {
            model.getModel()?.let { cubismModel ->
                projection.loadIdentity()

                if (cubismModel.canvasWidth > 1.0f && width < height) {
                    // 横向模型在纵向窗口中显示时，根据模型的宽度计算缩放
                    model.getModelMatrix().setWidth(2.0f)
                    projection.scale(1.0f, width.toFloat() / height.toFloat())
                } else {
                    projection.scale(height.toFloat() / width.toFloat(), 1.0f)
                }

                // 应用视图矩阵
                viewMatrix?.multiplyByMatrix(projection)

                // 模型绘制前回调
                LAppDelegate.getInstance().getView()?.preModelDraw(model)

                model.update()
                model.draw(projection)

                // 模型绘制后回调
                LAppDelegate.getInstance().getView()?.postModelDraw(model)
            } ?: run {
                if (LAppDefine.DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("Failed to get model.")
                }
            }
        }
    }

    /**
     * 处理拖动事件
     */
    fun onDrag(x: Float, y: Float) {
        models.forEach { model ->
            model.setDragging(x, y)
        }
    }

    /**
     * 处理点击事件
     */
    fun onTap(x: Float, y: Float) {
        if (LAppDefine.DEBUG_TOUCH_LOG_ENABLE) {
            LAppPal.printLog("tap point: {$x, y: $y}")
        }

        models.forEach { model ->
            // 点击头部时随机播放表情
            if (model.hitTest(LAppDefine.HitAreaName.HEAD.id, x, y)) {
                if (LAppDefine.DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("hit area: ${LAppDefine.HitAreaName.HEAD.id}")
                }
                model.setRandomExpression()
            }
            // 点击身体时随机播放动作
            else if (model.hitTest(LAppDefine.HitAreaName.BODY.id, x, y)) {
                if (LAppDefine.DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("hit area: ${LAppDefine.HitAreaName.BODY.id}")
                }
                model.startRandomMotion(
                    LAppDefine.MotionGroup.TAP_BODY.id,
                    LAppDefine.Priority.NORMAL.priority
                )
            }
        }
    }

    /**
     * 加载指定目录的模型
     */
    fun loadModel(modelPath: String) {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("Loading model: $modelPath")
        }

        // 释放当前模型
        releaseAllModel()

        // 从完整路径中提取目录和文件名
        val lastSlashIndex = modelPath.lastIndexOf('/')
        if (lastSlashIndex == -1) {
            throw IllegalArgumentException("Invalid model path format")
        }

        val dir = modelPath.substring(0, lastSlashIndex + 1)  // 包含最后的斜杠
        val fileName = modelPath.substring(lastSlashIndex + 1)
        
        // 创建并加载新模型
        val model = LAppModel()
        model.loadAssets(dir, fileName)
        models.add(model)

        // 更新当前模型索引
        currentModel = models.size - 1

        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("Model loaded: $fileName")
        }
    }

    fun getModel(number: Int): LAppModel? = models.getOrNull(number)
    fun getCurrentModel(): Int = currentModel
    fun getModelNum(): Int = models.size
} 