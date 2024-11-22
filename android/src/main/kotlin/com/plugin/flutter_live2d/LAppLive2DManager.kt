package com.plugin.flutter_live2d

import android.content.Context
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.motion.ACubismMotion
import com.live2d.sdk.cubism.framework.motion.IBeganMotionCallback
import com.live2d.sdk.cubism.framework.motion.IFinishedMotionCallback
import java.io.IOException

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
    private val modelDir = mutableListOf<String>()
    private var currentModel: Int = 0
    private val viewMatrix = CubismMatrix44.create()
    private val projection = CubismMatrix44.create()

    // 动作回调
    private val beganMotion = object : IBeganMotionCallback {
        override fun execute(motion: ACubismMotion) {
            LAppPal.printLog("Motion Began: $motion")
        }
    }

    private val finishedMotion = object : IFinishedMotionCallback {
        override fun execute(motion: ACubismMotion) {
            LAppPal.printLog("Motion Finished: $motion")
        }
    }

    init {
        setUpModel()
        changeScene(0)
    }

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
     * 设置模型目录
     */
    fun setUpModel() {
        modelDir.clear()

        try {
            val assets = LAppDelegate.getInstance().getContext()?.assets
                ?: throw IllegalStateException("Context is null")

            val root = assets.list("") ?: return
            for (subdir in root) {
                val files = assets.list(subdir) ?: continue
                val target = "$subdir.model3.json"
                
                // 查找同名的model3.json文件
                if (files.contains(target)) {
                    modelDir.add(subdir)
                }
            }
            modelDir.sort()
        } catch (ex: IOException) {
            throw IllegalStateException(ex)
        }
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
                LAppPal.printLog("Failed to get model.")
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
        if (LAppDefine.DEBUG_LOG_ENABLE) {
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
                    LAppDefine.Priority.NORMAL.priority,
                    finishedMotion,
                    beganMotion
                )
            }
        }
    }

    /**
     * 切换到下一个场景
     */
    fun nextScene() {
        val number = (currentModel + 1) % modelDir.size
        changeScene(number)
    }

    /**
     * 切换场景
     */
    fun changeScene(index: Int) {
        currentModel = index
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            LAppPal.printLog("model index: $currentModel")
        }

        val modelDirName = modelDir[index]
        val modelPath = "${LAppDefine.ResourcePath.ROOT.path}$modelDirName/"
        val modelJsonName = "$modelDirName.model3.json"

        releaseAllModel()

        // 加载主模型
        models.add(LAppModel().apply {
            loadAssets(modelPath, modelJsonName)
        })

        // 根据渲染目标设置处理透明度示例
        val useRenderingTarget = when {
            LAppDefine.USE_RENDER_TARGET -> LAppView.RenderingTarget.VIEW_FRAME_BUFFER
            LAppDefine.USE_MODEL_RENDER_TARGET -> LAppView.RenderingTarget.MODEL_FRAME_BUFFER
            else -> LAppView.RenderingTarget.NONE
        }

        // 如果使用渲染目标，创建第二个模型作为透明度示例
        if (LAppDefine.USE_RENDER_TARGET || LAppDefine.USE_MODEL_RENDER_TARGET) {
            models.add(LAppModel().apply {
                loadAssets(modelPath, modelJsonName)
                getModelMatrix().translateX(0.2f)
            })
        }

        // 切换渲染目标
        LAppDelegate.getInstance().getView()?.switchRenderingTarget(useRenderingTarget)

        // 设置渲染目标的清除颜色
        LAppDelegate.getInstance().getView()?.setRenderingTargetClearColor(0.0f, 0.0f, 0.0f)
    }

    fun getModel(number: Int): LAppModel? = models.getOrNull(number)
    fun getCurrentModel(): Int = currentModel
    fun getModelNum(): Int = models.size
} 