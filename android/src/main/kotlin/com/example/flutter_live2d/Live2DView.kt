package com.example.flutter_live2d

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.model.CubismModel
import com.live2d.sdk.cubism.framework.motion.CubismMotion
import com.live2d.sdk.cubism.framework.rendering.CubismRenderer

class Live2DView(context: Context) : View(context) {
    private var model: CubismModel? = null
    private var renderer: CubismRenderer? = null
    private var motionManager: CubismMotionManager? = null
    private var modelMatrix: CubismModelMatrix? = null
    
    private var scale = 1.0f
    private var positionX = 0.0f
    private var positionY = 0.0f

    init {
        CubismFramework.initialize()
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun loadModel(modelPath: String) {
        // 实现模型加载逻辑
        model = CubismModel.loadModel(modelPath)
        renderer = CubismRenderer.create(model)
        motionManager = CubismMotionManager()
        modelMatrix = CubismModelMatrix()
        
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        model?.let { model ->
            renderer?.let { renderer ->
                // 更新模型
                motionManager?.updateMotion(model)
                
                // 应用变换
                modelMatrix?.setPosition(positionX, positionY)
                modelMatrix?.setScale(scale, scale)
                
                // 渲染
                renderer.setMvpMatrix(modelMatrix?.matrix)
                renderer.drawModel()
            }
        }
        
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 处理触摸事件
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 触摸响应
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setScale(scale: Float) {
        this.scale = scale
    }

    fun setPosition(x: Float, y: Float) {
        this.positionX = x
        this.positionY = y
    }

    fun startMotion(group: String, index: Int) {
        motionManager?.startMotion(group, index)
    }

    fun setExpression(expression: String) {
        model?.setExpression(expression)
    }
} 