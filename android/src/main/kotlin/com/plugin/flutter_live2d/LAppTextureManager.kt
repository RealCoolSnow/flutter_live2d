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

            // First decode bounds to check size
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inScaled = false
                inPremultiplied = true
            }

            context.assets.open(fullPath).use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("Texture dimensions: ${options.outWidth}x${options.outHeight}")
            }

            // Create texture to ensure we have a GL context
            val textureId = IntArray(1)
            GLES20.glGenTextures(1, textureId, 0)
            
            // Now we can safely check max texture size
            val maxTextureSize = IntArray(1)
            GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0)
            
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("Max texture size: ${maxTextureSize[0]}")
            }

            // Check if texture is too large
            if (maxTextureSize[0] > 0 && (options.outWidth > maxTextureSize[0] || options.outHeight > maxTextureSize[0])) {
                GLES20.glDeleteTextures(1, textureId, 0)
                throw RuntimeException("Texture size (${options.outWidth}x${options.outHeight}) exceeds maximum supported size (${maxTextureSize[0]})")
            }

            // Now load the actual bitmap
            val bitmap = context.assets.open(fullPath).use { stream ->
                if (LAppDefine.DEBUG_LOG_ENABLE) {
                    LAppPal.printLog("Loading full texture")
                }
                
                options.inJustDecodeBounds = false
                BitmapFactory.decodeStream(stream, null, options)
            } ?: throw RuntimeException("Failed to decode bitmap from stream")

            if (LAppDefine.DEBUG_LOG_ENABLE) {
                LAppPal.printLog("Bitmap decoded: ${bitmap.width}x${bitmap.height}")
            }

            // Clear any existing GL errors before proceeding
            GLES20.glGetError()

            // 激活和绑定纹理
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])

            // Check for errors after binding
            val bindError = GLES20.glGetError()
            if (bindError != GLES20.GL_NO_ERROR) {
                GLES20.glDeleteTextures(1, textureId, 0)
                throw RuntimeException("Failed to bind texture, GL error: $bindError")
            }

            // Try with simpler texture parameters first
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            // Check for parameter errors
            val paramError = GLES20.glGetError()
            if (paramError != GLES20.GL_NO_ERROR) {
                GLES20.glDeleteTextures(1, textureId, 0)
                throw RuntimeException("Failed to set texture parameters, GL error: $paramError")
            }

            // 上传纹理数据
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            
            // Check for upload errors
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