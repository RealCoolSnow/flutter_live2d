package com.plugin.flutter_live2d

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils

class TextureManager(private val context: Context) {
    data class TextureInfo(
        val id: Int,
        val width: Int,
        val height: Int,
        val filePath: String
    )

    private val textures = mutableListOf<TextureInfo>()

    fun createTextureFromPngFile(filePath: String): TextureInfo {
        // 检查是否已加载
        textures.find { it.filePath == filePath }?.let { return it }

        try {
            println("TextureManager: Loading texture: $filePath")
            context.assets.open(filePath).use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    // 生成纹理
                    val textureId = IntArray(1)
                    GLES20.glGenTextures(1, textureId, 0)
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])

                    // 设置纹理参数
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

                    // 上传位图到纹理
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
                    GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)

                    val textureInfo = TextureInfo(
                        id = textureId[0],
                        width = bitmap.width,
                        height = bitmap.height,
                        filePath = filePath
                    )
                    textures.add(textureInfo)
                    bitmap.recycle()

                    println("TextureManager: Texture loaded: $textureInfo")
                    return textureInfo
                }
            }
        } catch (e: Exception) {
            println("TextureManager: Failed to load texture: $filePath")
            e.printStackTrace()
        }

        throw RuntimeException("Failed to load texture: $filePath")
    }
} 