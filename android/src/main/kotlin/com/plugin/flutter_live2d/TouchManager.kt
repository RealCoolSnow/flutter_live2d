package com.plugin.flutter_live2d

/**
 * 触摸管理器
 */
class TouchManager {
    // 触摸开始时的坐标
    private var startX: Float = 0.0f
    private var startY: Float = 0.0f
    
    // 单点触摸时的坐标
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    
    // 双点触摸时的坐标
    private var lastX1: Float = 0.0f
    private var lastY1: Float = 0.0f
    private var lastX2: Float = 0.0f
    private var lastY2: Float = 0.0f
    
    // 双点触摸时的指间距离
    private var lastTouchDistance: Float = -1.0f
    
    // 移动距离
    private var deltaX: Float = 0.0f
    private var deltaY: Float = 0.0f
    
    // 缩放比例
    private var scale: Float = 1.0f
    
    // 状态标记
    private var isTouchSingle: Boolean = true
    private var isFlipAvailable: Boolean = true

    /**
     * 触摸开始时的事件处理
     */
    fun touchesBegan(deviceX: Float, deviceY: Float) {
        lastX = deviceX
        lastY = deviceY
        startX = deviceX
        startY = deviceY
        lastTouchDistance = -1.0f
        isFlipAvailable = true
        isTouchSingle = true
    }

    /**
     * 单点触摸移动时的事件处理
     */
    fun touchesMoved(deviceX: Float, deviceY: Float) {
        lastX = deviceX
        lastY = deviceY
        lastTouchDistance = -1.0f
        isTouchSingle = true
    }

    /**
     * 双点触摸移动时的事件处理
     */
    fun touchesMoved(deviceX1: Float, deviceY1: Float, deviceX2: Float, deviceY2: Float) {
        val distance = calculateDistance(deviceX1, deviceY1, deviceX2, deviceY2)
        val centerX = (deviceX1 + deviceX2) * 0.5f
        val centerY = (deviceY1 + deviceY2) * 0.5f

        if (lastTouchDistance > 0.0f) {
            scale = Math.pow(distance / lastTouchDistance, 0.75).toFloat()
            deltaX = calculateMovingAmount(deviceX1 - lastX1, deviceX2 - lastX2)
            deltaY = calculateMovingAmount(deviceY1 - lastY1, deviceY2 - lastY2)
        } else {
            scale = 1.0f
            deltaX = 0.0f
            deltaY = 0.0f
        }

        lastX = centerX
        lastY = centerY
        lastX1 = deviceX1
        lastY1 = deviceY1
        lastX2 = deviceX2
        lastY2 = deviceY2
        lastTouchDistance = distance
        isTouchSingle = false
    }

    /**
     * 计算滑动距离
     */
    fun getFlickDistance(): Float = calculateDistance(startX, startY, lastX, lastY)

    /**
     * 计算两点间距离
     */
    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)).toDouble()).toFloat()
    }

    /**
     * 计算移动量
     * 如果两个值方向不同则返回0，相同则返回绝对值较小的值
     */
    private fun calculateMovingAmount(v1: Float, v2: Float): Float {
        if ((v1 > 0.0f) != (v2 > 0.0f)) {
            return 0.0f
        }

        val sign = if (v1 > 0.0f) 1.0f else -1.0f
        val absoluteValue1 = Math.abs(v1)
        val absoluteValue2 = Math.abs(v2)

        return sign * Math.min(absoluteValue1, absoluteValue2)
    }

    // Getters
    fun getStartX(): Float = startX
    fun getStartY(): Float = startY
    fun getLastX(): Float = lastX
    fun getLastY(): Float = lastY
    fun getLastX1(): Float = lastX1
    fun getLastY1(): Float = lastY1
    fun getLastX2(): Float = lastX2
    fun getLastY2(): Float = lastY2
    fun getLastTouchDistance(): Float = lastTouchDistance
    fun getDeltaX(): Float = deltaX
    fun getDeltaY(): Float = deltaY
    fun getScale(): Float = scale
    fun isTouchSingle(): Boolean = isTouchSingle
    fun isFlipAvailable(): Boolean = isFlipAvailable
}