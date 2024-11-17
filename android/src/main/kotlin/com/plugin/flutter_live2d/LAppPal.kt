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

    // fun moveTaskToBack() {
    //     LAppDelegate.getInstance().getActivity().moveTaskToBack(true)
    // }

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

    private fun getSystemNanoTime(): Double {
        return System.nanoTime().toDouble()
    }
} 