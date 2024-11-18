package com.plugin.flutter_live2d

class TouchManager {
    var startX: Float = 0.0f
        private set
    var startY: Float = 0.0f
        private set
    var lastX: Float = 0.0f
        private set
    var lastY: Float = 0.0f
        private set
    
    private var lastTouchDistance: Float = -1.0f
    private var isTouchSingle = true
    private var isFlipAvailable = true

    fun touchesBegan(deviceX: Float, deviceY: Float) {
        lastX = deviceX
        lastY = deviceY
        startX = deviceX
        startY = deviceY
        lastTouchDistance = -1.0f
        isFlipAvailable = true
        isTouchSingle = true
    }

    fun touchesMoved(deviceX: Float, deviceY: Float) {
        lastX = deviceX
        lastY = deviceY
        lastTouchDistance = -1.0f
        isTouchSingle = true
    }

    fun getFlickDistance(): Float {
        return calculateDistance(startX, startY, lastX, lastY)
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)).toDouble()).toFloat()
    }
}