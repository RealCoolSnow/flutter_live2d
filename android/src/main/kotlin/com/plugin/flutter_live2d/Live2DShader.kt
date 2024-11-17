package com.plugin.flutter_live2d

import android.content.Context
import android.opengl.GLES20
import android.util.Log

class Live2DShader(private val context: Context) {
    companion object {
        private const val TAG = "Live2DShader"
    }

    private var programId: Int = 0
    private var positionLocation: Int = 0
    private var uvLocation: Int = 0
    private var textureLocation: Int = 0
    private var baseColorLocation: Int = 0
    private var matrixLocation: Int = 0

    init {
        programId = createShader()
        if (programId > 0) {
            positionLocation = GLES20.glGetAttribLocation(programId, "position")
            uvLocation = GLES20.glGetAttribLocation(programId, "uv")
            textureLocation = GLES20.glGetUniformLocation(programId, "texture")
            baseColorLocation = GLES20.glGetUniformLocation(programId, "baseColor")
            matrixLocation = GLES20.glGetUniformLocation(programId, "matrix")

            println("Live2DShader: Locations - pos:$positionLocation uv:$uvLocation tex:$textureLocation color:$baseColorLocation matrix:$matrixLocation")
        }
    }

    fun getShaderId(): Int = programId

    private fun createShader(): Int {
        // 创建着色器
        val vertShaderFile = "Shaders/VertSprite.vert"
        val fragShaderFile = "Shaders/FragSprite.frag"

        // 编译着色器
        val vertexShaderId = compileShader(vertShaderFile, GLES20.GL_VERTEX_SHADER)
        val fragmentShaderId = compileShader(fragShaderFile, GLES20.GL_FRAGMENT_SHADER)

        if (vertexShaderId == 0 || fragmentShaderId == 0) {
            return 0
        }

        // 创建程序
        val programId = GLES20.glCreateProgram()
        if (programId == 0) {
            Log.e(TAG, "Failed to create program")
            return 0
        }

        // 附加着色器
        GLES20.glAttachShader(programId, vertexShaderId)
        GLES20.glAttachShader(programId, fragmentShaderId)

        // 绑定属性位置 - 必须在链接之前
        GLES20.glBindAttribLocation(programId, 0, "position")
        GLES20.glBindAttribLocation(programId, 1, "uv")

        // 链接程序
        GLES20.glLinkProgram(programId)

        // 检查链接状态
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            val log = GLES20.glGetProgramInfoLog(programId)
            Log.e(TAG, "Failed to link program: $log")
            GLES20.glDeleteProgram(programId)
            return 0
        }

        // 删除着色器
        GLES20.glDeleteShader(vertexShaderId)
        GLES20.glDeleteShader(fragmentShaderId)

        return programId
    }

    private fun compileShader(fileName: String, shaderType: Int): Int {
        try {
            // 打印文件名和着色器类型
            println("Live2DShader: Compiling shader - File: $fileName, Type: ${if (shaderType == GLES20.GL_VERTEX_SHADER) "Vertex" else "Fragment"}")
            
            // 尝试打开文件
            println("Live2DShader: Attempting to open shader file...")
            val shaderBuffer = try {
                context.assets.open(fileName).use { it.readBytes() }
            } catch (e: Exception) {
                println("Live2DShader: Failed to open shader file: ${e.message}")
                e.printStackTrace()
                return 0
            }
            
            // 打印着色器代码
            val shaderCode = String(shaderBuffer)
            println("Live2DShader: Shader code:\n$shaderCode")

            // 创建着色器
            println("Live2DShader: Creating shader...")
            val shaderId = GLES20.glCreateShader(shaderType)
            if (shaderId == 0) {
                println("Live2DShader: Failed to create shader object")
                return 0
            }
            println("Live2DShader: Created shader with ID: $shaderId")

            // 加载着色器源码
            println("Live2DShader: Loading shader source...")
            GLES20.glShaderSource(shaderId, shaderCode)
            
            // 检查OpenGL错误
            var error = GLES20.glGetError()
            if (error != GLES20.GL_NO_ERROR) {
                println("Live2DShader: Error after loading shader source: $error")
            }

            // 编译着色器
            println("Live2DShader: Compiling shader...")
            GLES20.glCompileShader(shaderId)
            
            // 检查OpenGL错误
            error = GLES20.glGetError()
            if (error != GLES20.GL_NO_ERROR) {
                println("Live2DShader: Error after compiling shader: $error")
            }

            // 获取编译状态
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
            println("Live2DShader: Compile status: ${compileStatus[0]}")

            // 总是获取并打印编译日志
            val log = GLES20.glGetShaderInfoLog(shaderId)
            if (log.isNotEmpty()) {
                println("Live2DShader: Compilation log: $log")
            }

            // 检查编译状态
            if (compileStatus[0] == GLES20.GL_FALSE) {
                println("Live2DShader: Shader compilation failed")
                GLES20.glDeleteShader(shaderId)
                return 0
            }

            println("Live2DShader: Shader compiled successfully")
            return shaderId
        } catch (e: Exception) {
            println("Live2DShader: Exception during shader compilation: ${e.message}")
            e.printStackTrace()
            return 0
        }
    }

    private fun checkShader(shaderId: Int): Boolean {
        // 获取日志长度
        val logLength = IntArray(1)
        GLES20.glGetShaderiv(shaderId, GLES20.GL_INFO_LOG_LENGTH, logLength, 0)

        if (logLength[0] > 0) {
            val log = GLES20.glGetShaderInfoLog(shaderId)
            Log.e(TAG, "Shader compile log: $log")
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

    fun dispose() {
        if (programId != 0) {
            GLES20.glDeleteProgram(programId)
            programId = 0
        }
    }

    fun getPositionLocation(): Int = positionLocation
    fun getUvLocation(): Int = uvLocation
    fun getTextureLocation(): Int = textureLocation
    fun getBaseColorLocation(): Int = baseColorLocation
    fun getMatrixLocation(): Int = matrixLocation
} 