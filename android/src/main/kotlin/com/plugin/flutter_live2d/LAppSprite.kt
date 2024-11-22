package com.plugin.flutter_live2d

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 精灵渲染类
 */
class LAppSprite(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    private val textureId: Int,
    programId: Int
) {
    private val rect = Rect()
    private val positionLocation: Int  // 位置属性
    private val uvLocation: Int        // UV属性
    private val textureLocation: Int   // 纹理属性
    private val colorLocation: Int     // 颜色属性
    private val spriteColor = FloatArray(4)  // 显示颜色
    private var maxWidth: Int = 1    // 窗口宽度
    private var maxHeight: Int = 1   // 窗口高度

    private var posVertexFloatBuffer: FloatBuffer? = null
    private var uvVertexFloatBuffer: FloatBuffer? = null
    private val uvVertex = FloatArray(8)
    private val positionVertex = FloatArray(8)

    init {
        // 设置矩形区域
        rect.left = x - width * 0.5f
        rect.right = x + width * 0.5f
        rect.up = y + height * 0.5f
        rect.down = y - height * 0.5f

        // 获取属性位置
        positionLocation = GLES20.glGetAttribLocation(programId, "position")
        uvLocation = GLES20.glGetAttribLocation(programId, "uv")
        textureLocation = GLES20.glGetUniformLocation(programId, "texture")
        colorLocation = GLES20.glGetUniformLocation(programId, "baseColor")

        // 设置默认颜色
        spriteColor[0] = 1.0f
        spriteColor[1] = 1.0f
        spriteColor[2] = 1.0f
        spriteColor[3] = 1.0f
    }

    /**
     * 渲染精灵
     */
    fun render() {
        // 设置UV坐标
        uvVertex[0] = 1.0f; uvVertex[1] = 0.0f
        uvVertex[2] = 0.0f; uvVertex[3] = 0.0f
        uvVertex[4] = 0.0f; uvVertex[5] = 1.0f
        uvVertex[6] = 1.0f; uvVertex[7] = 1.0f

        // 启用顶点属性
        GLES20.glEnableVertexAttribArray(positionLocation)
        GLES20.glEnableVertexAttribArray(uvLocation)

        // 设置纹理单元
        GLES20.glUniform1i(textureLocation, 0)

        // 计算顶点坐标
        val halfWidth = maxWidth * 0.5f
        val halfHeight = maxHeight * 0.5f
        positionVertex[0] = (rect.right - halfWidth) / halfWidth
        positionVertex[1] = (rect.up - halfHeight) / halfHeight
        positionVertex[2] = (rect.left - halfWidth) / halfWidth
        positionVertex[3] = (rect.up - halfHeight) / halfHeight
        positionVertex[4] = (rect.left - halfWidth) / halfWidth
        positionVertex[5] = (rect.down - halfHeight) / halfHeight
        positionVertex[6] = (rect.right - halfWidth) / halfWidth
        positionVertex[7] = (rect.down - halfHeight) / halfHeight

        // 创建和更新缓冲区
        updateBuffers()

        // 设置顶点属性
        GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 0, posVertexFloatBuffer)
        GLES20.glVertexAttribPointer(uvLocation, 2, GLES20.GL_FLOAT, false, 0, uvVertexFloatBuffer)

        // 设置颜色
        GLES20.glUniform4f(colorLocation, spriteColor[0], spriteColor[1], spriteColor[2], spriteColor[3])

        // 绑定纹理并绘制
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
    }

    /**
     * 使用指定纹理ID立即渲染
     */
    fun renderImmediate(textureId: Int, uvVertex: FloatArray) {
        // 启用属性
        GLES20.glEnableVertexAttribArray(positionLocation)
        GLES20.glEnableVertexAttribArray(uvLocation)

        // 设置纹理单元
        GLES20.glUniform1i(textureLocation, 0)

        // 计算顶点坐标
        val halfWidth = maxWidth * 0.5f
        val halfHeight = maxHeight * 0.5f
        val positionVertex = floatArrayOf(
            (rect.right - halfWidth) / halfWidth, (rect.up - halfHeight) / halfHeight,
            (rect.left - halfWidth) / halfWidth, (rect.up - halfHeight) / halfHeight,
            (rect.left - halfWidth) / halfWidth, (rect.down - halfHeight) / halfHeight,
            (rect.right - halfWidth) / halfWidth, (rect.down - halfHeight) / halfHeight
        )

        // 设置顶点属性
        ByteBuffer.allocateDirect(positionVertex.size * 4).apply {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(positionVertex)
                position(0)
                GLES20.glVertexAttribPointer(positionLocation, 2, GLES20.GL_FLOAT, false, 0, this)
            }
        }

        ByteBuffer.allocateDirect(uvVertex.size * 4).apply {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(uvVertex)
                position(0)
                GLES20.glVertexAttribPointer(uvLocation, 2, GLES20.GL_FLOAT, false, 0, this)
            }
        }

        // 设置颜色
        GLES20.glUniform4f(colorLocation, spriteColor[0], spriteColor[1], spriteColor[2], spriteColor[3])

        // 绑定纹理并绘制
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
    }

    /**
     * 更新缓冲区
     */
    private fun updateBuffers() {
        if (posVertexFloatBuffer == null) {
            posVertexFloatBuffer = ByteBuffer.allocateDirect(positionVertex.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer()
            }
        }
        if (uvVertexFloatBuffer == null) {
            uvVertexFloatBuffer = ByteBuffer.allocateDirect(uvVertex.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer()
            }
        }

        posVertexFloatBuffer?.put(positionVertex)?.position(0)
        uvVertexFloatBuffer?.put(uvVertex)?.position(0)
    }

    /**
     * 调整大小
     */
    fun resize(x: Float, y: Float, width: Float, height: Float) {
        rect.left = x - width * 0.5f
        rect.right = x + width * 0.5f
        rect.up = y + height * 0.5f
        rect.down = y - height * 0.5f
    }

    /**
     * 碰撞检测
     */
    fun isHit(pointX: Float, pointY: Float): Boolean {
        val y = maxHeight - pointY
        return (pointX >= rect.left && pointX <= rect.right && y <= rect.up && y >= rect.down)
    }

    /**
     * 设置颜色
     */
    fun setColor(r: Float, g: Float, b: Float, a: Float) {
        spriteColor[0] = r
        spriteColor[1] = g
        spriteColor[2] = b
        spriteColor[3] = a
    }

    /**
     * 设置窗口大小
     */
    fun setWindowSize(width: Int, height: Int) {
        maxWidth = width
        maxHeight = height
    }

    /**
     * 矩形区域数据类
     */
    private data class Rect(
        var left: Float = 0f,   // 左边界
        var right: Float = 0f,  // 右边界
        var up: Float = 0f,     // 上边界
        var down: Float = 0f    // 下边界
    )
}