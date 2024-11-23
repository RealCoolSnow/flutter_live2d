package com.plugin.flutter_live2d

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import java.io.IOException

class LAppWavFileHandler(private val filePath: String) : Thread() {

    override fun run() {
        loadWavFile()
    }

    private fun loadWavFile() {
        // API 24以下不支持,直接返回
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return
        }

        val mediaExtractor = MediaExtractor()
        try {
            val afd = LAppDelegate.getInstance().getContext()?.assets?.openFd(filePath)
                ?: throw IOException("Failed to open asset file: $filePath")
            mediaExtractor.setDataSource(afd)
        } catch (e: IOException) {
            // 发生异常时打印错误并返回
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("Failed to load wav file: ${e.message}")
            }
            e.printStackTrace()
            return
        }

        val mediaFormat = mediaExtractor.getTrackFormat(0)
        val samplingRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)

        val bufferSize = AudioTrack.getMinBufferSize(
            samplingRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(samplingRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()

        audioTrack.play()

        // 避免断断续续的声音
        val offset = 100
        val voiceBuffer = LAppPal.loadFileAsBytes(filePath)
        audioTrack.write(voiceBuffer, offset, voiceBuffer.size - offset)
    }
} 