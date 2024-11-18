package com.plugin.flutter_live2d

import android.content.Context
import android.opengl.GLES20
import android.util.Log

class LAppSpriteShader(private val context: Context) {
    companion object {
        private const val TAG = "LAppSpriteShader"
        private const val SHADER_PATH = "Shaders"
        private const val VERT_SHADER = "VertSprite.vert"
        private const val FRAG_SHADER = "FragSprite.frag"
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

            println("LAppSpriteShader: Locations - pos:$positionLocation uv:$uvLocation tex:$textureLocation color:$baseColorLocation matrix:$matrixLocation")
        }
    }

    private fun createShader(): Int {
        try {
            // 编译顶点着色器
            Log.d(TAG, "Compiling vertex shader...")
            val vertShaderPath = "$SHADER_PATH/$VERT_SHADER"
            val vertexShaderId = compileShader(vertShaderPath, GLES20.GL_VERTEX_SHADER)
            if (vertexShaderId == 0) {
                Log.e(TAG, "Failed to compile vertex shader")
                return 0
            }

            // 编译片段着色器
            Log.d(TAG, "Compiling fragment shader...")
            val fragShaderPath = "$SHADER_PATH/$FRAG_SHADER"
            val fragmentShaderId = compileShader(fragShaderPath, GLES20.GL_FRAGMENT_SHADER)
            if (fragmentShaderId == 0) {
                GLES20.glDeleteShader(vertexShaderId)
                Log.e(TAG, "Failed to compile fragment shader")
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

            // 绑定属性位置
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
        } catch (e: Exception) {
            Log.e(TAG, "Error creating shader program", e)
            return 0
        }
    }

    private fun compileShader(fileName: String, shaderType: Int): Int {
        try {
            // 从assets加载着色器文件
            Log.d(TAG, "Loading shader file: $fileName")
            val shaderCode = context.assets.open(fileName).bufferedReader().use { it.readText() }
            Log.d(TAG, "Shader code loaded:\n$shaderCode")

            if (shaderCode.isEmpty()) {
                Log.e(TAG, "Shader code is empty")
                return 0
            }

            // 创建着色器
            val shaderId = GLES20.glCreateShader(shaderType)
            if (shaderId == 0) {
                Log.e(TAG, "Failed to create shader type: $shaderType")
                return 0
            }

            // 加载着色器源码并编译
            GLES20.glShaderSource(shaderId, shaderCode)
            GLES20.glCompileShader(shaderId)

            // 检查编译状态
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            // 获取编译日志
            val log = GLES20.glGetShaderInfoLog(shaderId)
            if (log.isNotEmpty()) {
                Log.d(TAG, "Shader compile log: $log")
            }

            if (compileStatus[0] == GLES20.GL_FALSE) {
                Log.e(TAG, "Shader compilation failed")
                GLES20.glDeleteShader(shaderId)
                return 0
            }

            return shaderId
        } catch (e: Exception) {
            Log.e(TAG, "Error loading shader file: ${e.message}")
            e.printStackTrace()
            return 0
        }
    }

    fun dispose() {
        if (programId != 0) {
            GLES20.glDeleteProgram(programId)
            programId = 0
        }
    }

    fun getShaderId(): Int = programId
    fun getPositionLocation(): Int = positionLocation
    fun getUvLocation(): Int = uvLocation
    fun getTextureLocation(): Int = textureLocation
    fun getBaseColorLocation(): Int = baseColorLocation
    fun getMatrixLocation(): Int = matrixLocation
} 