package com.plugin.flutter_live2d

import android.content.Context
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
            // 从assets文件夹加载图片并创建位图
            context.assets.open(filePath).use { stream ->
                // decodeStream会将图片读取为预乘alpha格式
                val bitmap = BitmapFactory.decodeStream(stream) ?: throw RuntimeException("Failed to decode bitmap")

                // 激活Texture0
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

                // 生成OpenGL纹理
                val textureId = IntArray(1)
                GLES20.glGenTextures(1, textureId, 0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])

                // 将内存中的2D图像分配给纹理
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

                // 生成mipmap
                GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)

                // 设置缩小时的插值
                GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR_MIPMAP_LINEAR
                )
                // 设置放大时的插值
                GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR
                )

                val textureInfo = TextureInfo(
                    id = textureId[0],
                    width = bitmap.width,
                    height = bitmap.height,
                    filePath = filePath
                )

                textures.add(textureInfo)

                // 释放位图
                bitmap.recycle()

                if (LAppDefine.DEBUG_LOG_ENABLE) {
                    CubismFramework.coreLogFunction("Created texture: $filePath")
                }

                return textureInfo
            }
        } catch (e: Exception) {
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                CubismFramework.coreLogFunction("Failed to create texture: $filePath")
                e.printStackTrace()
            }
            throw RuntimeException("Failed to create texture: $filePath", e)
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