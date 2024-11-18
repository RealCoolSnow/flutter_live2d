package com.plugin.flutter_live2d

import android.util.Log
import com.live2d.sdk.cubism.core.ICubismLogger

object LAppPal {
    private const val TAG = "[APP]"
    private var s_currentFrame = 0.0
    private var _lastNanoTime = 0.0
    private var _deltaNanoTime = 0.0

    class PrintLogFunction : ICubismLogger {
        override fun print(message: String) {
            Log.d(TAG, message)
        }
    }

    fun updateTime() {
        s_currentFrame = getSystemNanoTime()
        _deltaNanoTime = s_currentFrame - _lastNanoTime
        _lastNanoTime = s_currentFrame
    }

    fun getDeltaTime(): Float {
        return (_deltaNanoTime / 1000000000.0f).toFloat()
    }

    fun printLog(message: String) {
        Log.d(TAG, message)
    }

    fun loadFileAsBytes(path: String): ByteArray {
        try {
            val delegate = LAppDelegate.getInstance()
            val context = delegate.getContext() ?: throw RuntimeException("Context is null")
            
            context.assets.open(path).use { inputStream ->
                return inputStream.readBytes()
            }
        } catch (e: Exception) {
            printLog("Failed to load file: $path")
            e.printStackTrace()
            return ByteArray(0)
        }
    }

    private fun getSystemNanoTime(): Double {
        return System.nanoTime().toDouble()
    }
} 