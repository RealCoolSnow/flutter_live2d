package com.plugin.flutter_live2d

import android.content.Context
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.math.CubismViewMatrix

class Live2DManager {
    private var model: Live2DModel? = null
    private val viewMatrix = CubismViewMatrix()
    private val deviceToScreen = CubismMatrix44.create()
    private val projection = CubismMatrix44.create()
    private var windowWidth = 0
    private var windowHeight = 0

    fun loadModel(context: Context, modelPath: String) {
        println("Live2DManager: Loading model: $modelPath")
        try {
            val lastSlash = modelPath.lastIndexOf('/')
            val dir = modelPath.substring(0, lastSlash + 1)
            val fileName = modelPath.substring(lastSlash + 1)
            
            // 释放旧模型
            model?.dispose()
            
            // 创建新模型
            model = Live2DModel(context).apply {
                loadAssets(dir, fileName)
            }
            
            // 更新投影矩阵
            updateProjection()
            
            println("Live2DManager: Model loaded successfully")
        } catch (e: Exception) {
            println("Live2DManager: Failed to load model")
            e.printStackTrace()
        }
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        println("Live2DManager: Surface changed width: $width, height: $height")
        windowWidth = width
        windowHeight = height
        updateProjection()
    }

    fun onUpdate() {
        model?.let { model ->
            println("Live2DManager: Updating model")
            // 更新模型
            model.update()
            
            // 绘制模型
            projection.loadIdentity()
            println("Live2DManager: Model canvas width: ${model.getModel()?.getCanvasWidth()}")
            if (model.getModel()?.getCanvasWidth() ?: 0f > 1.0f && windowWidth < windowHeight) {
                // 横向模型在竖屏显示时的处理
                println("Live2DManager: Adjusting for portrait mode")
                model.getModelMatrix()?.scale(2.0f, 2.0f)
                projection.scale(1.0f, windowWidth.toFloat() / windowHeight.toFloat())
            } else {
                println("Live2DManager: Using default scaling")
                projection.scale(windowHeight.toFloat() / windowWidth.toFloat(), 1.0f)
            }
            
            println("Live2DManager: Drawing model")
            model.draw(projection)
        } ?: run {
            println("Live2DManager: No model to update")
        }
    }

    fun onDrag(x: Float, y: Float) {
        model?.setDragging(x, y)
    }

    fun onTap(x: Float, y: Float) {
        model?.let { model ->
            // 检查头部点击
            if (model.hitTest("Head", x, y)) {
                model.setRandomExpression()
            }
            // 检查身体点击
            else if (model.hitTest("Body", x, y)) {
                model.startRandomMotion("TapBody", 0)
            }
        }
    }

    private fun updateProjection() {
        model?.let { model ->
            projection.loadIdentity()
            
            // 根据模型和屏幕尺寸调整投影
            if (model.getModel()?.getCanvasWidth() ?: 0f > 1.0f && windowWidth < windowHeight) {
                model.getModelMatrix()?.scale(2.0f, 2.0f)
                projection.scale(1.0f, windowWidth.toFloat() / windowHeight.toFloat())
            } else {
                projection.scale(windowHeight.toFloat() / windowWidth.toFloat(), 1.0f)
            }
        }
    }

    fun setScale(scale: Float) {
        model?.setScale(scale)
    }

    fun setPosition(x: Float, y: Float) {
        model?.setPosition(x, y)
    }

    fun startMotion(group: String, index: Int) {
        model?.startMotion(group, index)
    }

    fun setExpression(expression: String) {
        model?.setExpression(expression)
    }

    fun isModelLoaded(): Boolean {
        return model != null
    }
} 