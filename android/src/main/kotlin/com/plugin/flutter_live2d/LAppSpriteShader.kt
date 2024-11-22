package com.plugin.flutter_live2d

import android.opengl.GLES20
import com.live2d.sdk.cubism.framework.utils.CubismDebug

/**
 * 精灵着色器设置类
 */
class LAppSpriteShader : AutoCloseable {
    private val programId: Int

    init {
        programId = createShader()
    }

    override fun close() {
        GLES20.glDeleteShader(programId)
    }

    /**
     * 获取着色器ID
     */
    fun getShaderId(): Int = programId

    /**
     * 创建着色器
     * @return 着色器ID，创建失败时返回0
     */
    private fun createShader(): Int {
        // 创建着色器路径
        val vertShaderFile = LAppDefine.ResourcePath.SHADER_ROOT.path + "/" + 
                            LAppDefine.ResourcePath.VERT_SHADER.path
        val fragShaderFile = LAppDefine.ResourcePath.SHADER_ROOT.path + "/" + 
                            LAppDefine.ResourcePath.FRAG_SHADER.path

        // 编译着色器
        val vertexShaderId = compileShader(vertShaderFile, GLES20.GL_VERTEX_SHADER)
        val fragmentShaderId = compileShader(fragShaderFile, GLES20.GL_FRAGMENT_SHADER)

        if (vertexShaderId == 0 || fragmentShaderId == 0) {
            return 0
        }

        // 创建程序对象
        val programId = GLES20.glCreateProgram()

        // 设置程序的着色器
        GLES20.glAttachShader(programId, vertexShaderId)
        GLES20.glAttachShader(programId, fragmentShaderId)

        GLES20.glLinkProgram(programId)
        GLES20.glUseProgram(programId)

        // 删除不再需要的着色器对象
        GLES20.glDeleteShader(vertexShaderId)
        GLES20.glDeleteShader(fragmentShaderId)

        return programId
    }

    /**
     * 检查着色器编译状态
     * @param shaderId 着色器ID
     * @return 检查结果，true表示无错误
     */
    private fun checkShader(shaderId: Int): Boolean {
        // 获取日志长度
        val logLength = IntArray(1)
        GLES20.glGetShaderiv(shaderId, GLES20.GL_INFO_LOG_LENGTH, logLength, 0)

        if (logLength[0] > 0) {
            val log = GLES20.glGetShaderInfoLog(shaderId)
            CubismDebug.cubismLogError("Shader compile log: $log")
        }

        // 获取编译状态
        val status = IntArray(1)
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, status, 0)

        if (status[0] == GLES20.GL_FALSE) {
            GLES20.glDeleteShader(shaderId)
            return false
        }

        return true
    }

    /**
     * 编译着色器
     * @param fileName 着色器文件名
     * @param shaderType 要创建的着色器类型
     * @return 着色器ID，创建失败时返回0
     */
    private fun compileShader(fileName: String, shaderType: Int): Int {
        try {
            // 读取文件
            val shaderBuffer = LAppPal.loadFileAsBytes(fileName)

            // 编译
            val shaderId = GLES20.glCreateShader(shaderType)
            GLES20.glShaderSource(shaderId, String(shaderBuffer))
            GLES20.glCompileShader(shaderId)

            if (!checkShader(shaderId)) {
                return 0
            }

            return shaderId
        } catch (e: Exception) {
            if (LAppDefine.DEBUG_LOG_ENABLE) {
                CubismDebug.cubismLogError("Failed to compile shader: ${e.message}")
            }
            return 0
        }
    }

    companion object {
        // 着色器相关常量
        private const val POSITION_ATTRIBUTE = "position"
        private const val UV_ATTRIBUTE = "uv"
        private const val TEXTURE_UNIFORM = "texture"
        private const val MATRIX_UNIFORM = "matrix"
        private const val BASE_COLOR_UNIFORM = "baseColor"
    }
} 