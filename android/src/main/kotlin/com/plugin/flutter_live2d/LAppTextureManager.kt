package com.plugin.flutter_live2d

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.live2d.sdk.cubism.framework.CubismFramework

/**
 * 纹理管理类
 */
class LAppTextureManager(private val context: Context) {
    /**
     * 纹理信息数据类
     */
    data class TextureInfo(
        var id: Int = 0,          // 纹理ID
        var width: Int = 0,       // 宽度
        var height: Int = 0,      // 高度
        var filePath: String = "" // 文件路径
    )

    // 纹理信息列表
    private val textures = mutableListOf<TextureInfo>()

    /**
     * 从PNG文件创建纹理
     */
    fun createTextureFromPngFile(filePath: String): TextureInfo {
        // 检查是否已加载过该纹理
        textures.find { it.filePath == filePath }?.let { 
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                CubismFramework.coreLogFunction("Texture already exists: $filePath")
            }
            return it 
        }

        try {
            val fullPath = LAppDefine.PathUtils.ensureFlutterAssetsPath(filePath)
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("Loading texture from: $fullPath")
            }

            context.assets.open(fullPath).use { stream ->
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inScaled = false
                    inPremultiplied = false  // 添加这行，禁用预乘alpha
                }
                
                val bitmap = BitmapFactory.decodeStream(stream)
                    ?: throw RuntimeException("Failed to decode bitmap")

                if (LAppDefine.DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("Bitmap decoded: ${bitmap.width}x${bitmap.height}")
                }
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                // 生成纹理
                val textureId = IntArray(1)
                GLES20.glGenTextures(1, textureId, 0)
                
                // 检查纹理生成
                if (textureId[0] == 0) {
                    val error = GLES20.glGetError()
                    throw RuntimeException("Failed to generate texture, GL error: $error")
                }

                // 激活和绑定纹理
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])

                // 设置纹理参数
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

                // 上传纹理数据
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0)  // 指定 GL_RGBA 格式

                // 检查纹理上传
                val uploadError = GLES20.glGetError()
                if (uploadError != GLES20.GL_NO_ERROR) {
                    GLES20.glDeleteTextures(1, textureId, 0)
                    throw RuntimeException("Failed to upload texture data, GL error: $uploadError")
                }

                val textureInfo = TextureInfo(
                    id = textureId[0],
                    width = bitmap.width,
                    height = bitmap.height,
                    filePath = filePath
                )

                // 释放位图
                bitmap.recycle()

                // 添加到纹理列表
                textures.add(textureInfo)

                if (LAppDefine.DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("Texture created successfully: ID=${textureInfo.id}")
                }

                return textureInfo
            }
        } catch (e: Exception) {
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("Failed to create texture: ${e.message}")
                e.printStackTrace()
            }
            throw e
        }
    }

    /**
     * 释放所有纹理
     */
    fun release() {
        textures.forEach { textureInfo ->
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                CubismFramework.coreLogFunction("Deleting texture: ${textureInfo.filePath}")
            }
            GLES20.glDeleteTextures(1, intArrayOf(textureInfo.id), 0)
        }
        textures.clear()
    }

    /**
     * 根据纹理ID获取纹理信息
     */
    fun getTextureById(textureId: Int): TextureInfo? {
        return textures.find { it.id == textureId }
    }

    /**
     * 根据文件路径获取纹理信息
     */
    fun getTextureByPath(filePath: String): TextureInfo? {
        return textures.find { it.filePath == filePath }
    }
} 