package com.plugin.flutter_live2d

import android.content.Context
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.math.CubismViewMatrix

class LAppLive2DManager(private val context: Context) {
    companion object {
        private var instance: LAppLive2DManager? = null
        
        fun getInstance(context: Context): LAppLive2DManager {
            if (instance == null) {
                instance = LAppLive2DManager(context)
            }
            return instance!!
        }

        fun releaseInstance() {
            instance = null
        }
    }

    private var model: LAppModel? = null
    private val projection = CubismMatrix44.create()
    private val viewMatrix = CubismMatrix44.create()

    fun loadModel(modelPath: String) {
        println("LAppLive2DManager: Loading model from $modelPath")
        releaseModel()
        
        // 创建新模型
        model = LAppModel(context)
        model?.loadAssets(getModelDir(modelPath), getModelFile(modelPath))
    }

    fun setScale(scale: Float) {
        model?.setScale(scale)
    }

    fun setPosition(x: Float, y: Float) {
        model?.setPosition(x, y)
    }

    fun startMotion(group: String, index: Int) {
        model?.startMotion(group, index, LAppDefine.Priority.NORMAL.priority)
    }

    fun setExpression(expression: String) {
        model?.setExpression(expression)
    }

    fun onUpdate() {
        val width = LAppDelegate.getInstance().getWindowWidth()
        val height = LAppDelegate.getInstance().getWindowHeight()

        // 更新投影矩阵
        projection.loadIdentity()
        if (model?.getModel()?.canvasWidth ?: 0f > 1.0f && width < height) {
            model?.getModelMatrix()?.setWidth(2.0f)
            projection.scale(1.0f, width.toFloat() / height.toFloat())
        } else {
            projection.scale(height.toFloat() / width.toFloat(), 1.0f)
        }

        // 更新视图矩阵
        viewMatrix?.let {
            it.multiplyByMatrix(projection)
        }

        // 更新模型
        model?.update()
        model?.draw(projection)
    }

    fun onDrag(x: Float, y: Float) {
        model?.setDragging(x, y)
    }

    fun onTap(x: Float, y: Float) {
        model?.hitTest(LAppDefine.HitAreaName.BODY.id, x, y)?.let {
            model?.startRandomMotion(LAppDefine.MotionGroup.TAP_BODY.id, LAppDefine.Priority.NORMAL.priority)
        }
    }

    private fun releaseModel() {
        model?.release()
        model = null
    }

    private fun getModelDir(modelPath: String): String {
        return modelPath.substringBeforeLast("/") + "/"
    }

    private fun getModelFile(modelPath: String): String {
        return modelPath.substringAfterLast("/")
    }
} 