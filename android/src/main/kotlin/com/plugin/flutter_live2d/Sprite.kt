package com.plugin.flutter_live2d

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Sprite(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    private val textureId: Int,
    private val programId: Int
) {
    private val rect = Rect()
    private var positionLocation: Int = 0
    private var uvLocation: Int = 0
    private var textureLocation: Int = 0
    private var colorLocation: Int = 0
    private val spriteColor = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
    private var maxWidth: Int = 1
    private var maxHeight: Int = 1
    private var posVertexFloatBuffer: FloatBuffer? = null
    private var uvVertexFloatBuffer: FloatBuffer? = null
    private val uvVertex = FloatArray(8)
    private val positionVertex = FloatArray(8)

    init {
        rect.left = x - width * 0.5f
        rect.right = x + width * 0.5f
        rect.up = y + height * 0.5f
        rect.down = y - height * 0.5f

        // 获取属性和统一变量位置
        positionLocation = GLES20.glGetAttribLocation(programId, "position")
        uvLocation = GLES20.glGetAttribLocation(programId, "uv")
        textureLocation = GLES20.glGetUniformLocation(programId, "texture")
        colorLocation = GLES20.glGetUniformLocation(programId, "baseColor")
    }

    fun render() {
        // 设置UV坐标
        uvVertex[0] = 1.0f
        uvVertex[1] = 0.0f
        uvVertex[2] = 0.0f
        uvVertex[3] = 0.0f
        uvVertex[4] = 0.0f
        uvVertex[5] = 1.0f
        uvVertex[6] = 1.0f
        uvVertex[7] = 1.0f

        // 启用顶点属性
        GLES20.glEnableVertexAttribArray(positionLocation)
        GLES20.glEnableVertexAttribArray(uvLocation)

        // 设置纹理
        GLES20.glUniform1i(textureLocation, 0)

        // 设置顶点坐标
        positionVertex[0] = (rect.right - maxWidth * 0.5f) / (maxWidth * 0.5f)
        positionVertex[1] = (rect.up - maxHeight * 0.5f) / (maxHeight * 0.5f)
        positionVertex[2] = (rect.left - maxWidth * 0.5f) / (maxWidth * 0.5f)
        positionVertex[3] = (rect.up - maxHeight * 0.5f) / (maxHeight * 0.5f)
        positionVertex[4] = (rect.left - maxWidth * 0.5f) / (maxWidth * 0.5f)
        positionVertex[5] = (rect.down - maxHeight * 0.5f) / (maxHeight * 0.5f)
        positionVertex[6] = (rect.right - maxWidth * 0.5f) / (maxWidth * 0.5f)
        positionVertex[7] = (rect.down - maxHeight * 0.5f) / (maxHeight * 0.5f)

        // 创建缓冲区
        if (posVertexFloatBuffer == null) {
            val posVertexByteBuffer = ByteBuffer.allocateDirect(positionVertex.size * 4)
            posVertexByteBuffer.order(ByteOrder.nativeOrder())
            posVertexFloatBuffer = posVertexByteBuffer.asFloatBuffer()
        }
        if (uvVertexFloatBuffer == null) {
            val uvVertexByteBuffer = ByteBuffer.allocateDirect(uvVertex.size * 4)
            uvVertexByteBuffer.order(ByteOrder.nativeOrder())
            uvVertexFloatBuffer = uvVertexByteBuffer.asFloatBuffer()
        }

        // 更新缓冲区数据
        posVertexFloatBuffer?.put(positionVertex)?.position(0)
        uvVertexFloatBuffer?.put(uvVertex)?.position(0)

        // 设置顶点属性
        GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 0, posVertexFloatBuffer)
        GLES20.glVertexAttribPointer(uvLocation, 2, GLES20.GL_FLOAT, false, 0, uvVertexFloatBuffer)

        // 设置颜色
        GLES20.glUniform4f(colorLocation, spriteColor[0], spriteColor[1], spriteColor[2], spriteColor[3])

        // 绑定纹理并绘制
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
    }

    fun setColor(r: Float, g: Float, b: Float, a: Float) {
        spriteColor[0] = r
        spriteColor[1] = g
        spriteColor[2] = b
        spriteColor[3] = a
    }

    fun setWindowSize(width: Int, height: Int) {
        maxWidth = width
        maxHeight = height
    }

    fun resize(x: Float, y: Float, width: Float, height: Float) {
        rect.left = x - width * 0.5f
        rect.right = x + width * 0.5f
        rect.up = y + height * 0.5f
        rect.down = y - height * 0.5f
    }

    fun isHit(pointX: Float, pointY: Float): Boolean {
        // y坐标需要转换
        val y = maxHeight - pointY
        return (pointX >= rect.left && pointX <= rect.right && y <= rect.up && y >= rect.down)
    }

    private data class Rect(
        var left: Float = 0f,
        var right: Float = 0f,
        var up: Float = 0f,
        var down: Float = 0f
    )
} 