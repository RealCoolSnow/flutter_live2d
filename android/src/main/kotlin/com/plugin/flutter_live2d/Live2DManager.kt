package com.plugin.flutter_live2d

import android.content.Context
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.math.CubismViewMatrix

class Live2DManager {
    private var model: Live2DModel? = null
    private val viewMatrix = CubismViewMatrix()
    private val deviceToScreen = CubismMatrix44.create()
    private val projection = CubismMatrix44.create()

    fun loadModel(context: Context, modelPath: String) {
        println("Live2DManager: Loading model: $modelPath")
        val lastSlash = modelPath.lastIndexOf('/')
        val dir = modelPath.substring(0, lastSlash + 1)
        
        model = Live2DModel(context)
        model?.loadAssets(dir, modelPath.substring(lastSlash + 1))
        println("Live2DManager: Model loaded")
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        println("Live2DManager: Surface changed: $width x $height")
        
        // Set view matrix
        val ratio = width.toFloat() / height.toFloat()
        val left = -ratio
        val right = ratio
        val bottom = -1.0f
        val top = 1.0f

        viewMatrix.setScreenRect(left, right, bottom, top)
        viewMatrix.scale(1.0f, 1.0f)

        // Set maximum/minimum scale
        viewMatrix.setMaxScale(2.0f)
        viewMatrix.setMinScale(0.8f)

        // Set maximum screen range
        viewMatrix.setMaxScreenRect(
            -2.0f,
            2.0f,
            -2.0f,
            2.0f
        )

        // Initialize device to screen matrix
        deviceToScreen.loadIdentity()
        if (width > height) {
            val screenW = Math.abs(right - left)
            deviceToScreen.scaleRelative(screenW / width, -screenW / width)
        } else {
            val screenH = Math.abs(top - bottom)
            deviceToScreen.scaleRelative(screenH / height, -screenH / height)
        }
        deviceToScreen.translateRelative(-width * 0.5f, -height * 0.5f)

        // Update projection matrix
        projection.loadIdentity()
        if (model?.getModel()?.getCanvasWidth() ?: 0f > 1.0f && width < height) {
            model?.getModelMatrix()?.scale(2.0f, 2.0f)
            projection.scale(1.0f, width.toFloat() / height.toFloat())
        } else {
            projection.scale(height.toFloat() / width.toFloat(), 1.0f)
        }
    }

    fun onUpdate() {
        model?.let { model ->
            model.update()
            model.draw(projection)
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

    fun getViewMatrix(): CubismViewMatrix = viewMatrix
    fun getDeviceToScreenMatrix(): CubismMatrix44 = deviceToScreen

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

    fun setOpacity(opacity: Float) {
        model?.setOpacity(opacity)
    }

    fun isModelLoaded(): Boolean {
        return model != null
    }
} 