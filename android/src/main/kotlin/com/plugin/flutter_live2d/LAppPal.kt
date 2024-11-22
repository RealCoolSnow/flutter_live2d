package com.plugin.flutter_live2d

import android.util.Log
import com.live2d.sdk.cubism.core.ICubismLogger
import java.io.IOException

/**
 * 应用程序工具类
 */
object LAppPal {
    private const val TAG = "[APP]"
    private var s_currentFrame = 0.0
    private var _lastNanoTime = 0.0
    private var _deltaNanoTime = 0.0

    /**
     * 用于在CubismFramework中注册的日志记录功能类
     */
    class PrintLogFunction : ICubismLogger {
        override fun print(message: String) {
            Log.d(TAG, message)
        }
    }

    /**
     * 将应用程序移至后台。执行时会触发onPause()事件
     */
    fun moveTaskToBack() {
        LAppDelegate.getInstance().getContext()?.let { context ->
            // 在Android中，需要Activity上下文才能调用moveTaskToBack
            // 如果context不是Activity，这里可能需要其他处理方式
        }
    }

    /**
     * 更新时间
     */
    fun updateTime() {
        s_currentFrame = getSystemNanoTime()
        _deltaNanoTime = s_currentFrame - _lastNanoTime
        _lastNanoTime = s_currentFrame
    }

    /**
     * 将文件读取为字节数组
     */
    fun loadFileAsBytes(path: String): ByteArray {
        try {
            LAppDelegate.getInstance().getContext()?.assets?.open(path)?.use { fileData ->
                val fileSize = fileData.available()
                val fileBuffer = ByteArray(fileSize)
                fileData.read(fileBuffer, 0, fileSize)
                return fileBuffer
            } ?: throw IOException("Failed to open file: $path")
        } catch (e: IOException) {
            e.printStackTrace()
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                printLog("File open error: ${e.message}")
            }
            return ByteArray(0)
        }
    }

    /**
     * 获取增量时间（与上一帧的时间差）
     */
    fun getDeltaTime(): Float {
        // 将纳秒转换为秒
        return (_deltaNanoTime / 1000000000.0f).toFloat()
    }

    /**
     * 日志记录功能
     */
    fun printLog(message: String) {
        if (LAppDefine.DEBUG_LOG_ENABLE) {
            Log.d(TAG, message)
        }
    }

    /**
     * 获取系统纳秒时间
     */
    private fun getSystemNanoTime(): Double {
        return System.nanoTime().toDouble()
    }
} 