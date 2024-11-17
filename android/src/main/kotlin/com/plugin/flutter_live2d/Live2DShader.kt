package com.plugin.flutter_live2d

import android.content.Context
import android.opengl.GLES20
import android.util.Log

class Live2DShader(private val context: Context) {
    companion object {
        private const val TAG = "Live2DShader"
        
        // 从demo中的VertSprite.vert复制
        private const val VERTEX_SHADER = """
            #version 100
            
            attribute vec3 position;
            attribute vec2 uv;
            varying vec2 vuv;
            
            void main(void)
            {
                gl_Position = vec4(position, 1.0);
                vuv = uv;
            }
        """

        // 从demo中的FragSprite.frag复制
        private const val FRAGMENT_SHADER = """
            #version 100
            
            precision mediump float;
            
            varying vec2 vuv;
            uniform sampler2D texture;
            uniform vec4 baseColor;
            
            void main(void)
            {
                gl_FragColor = texture2D(texture, vuv) * baseColor;
            }
        """
    }

    private var programId: Int = 0

    init {
        programId = createShader()
    }

    fun getShaderId(): Int = programId

    private fun createShader(): Int {
        try {
            // 编译着色器
            val vertexShaderId = compileShader(VERTEX_SHADER, GLES20.GL_VERTEX_SHADER)
            if (vertexShaderId == 0) {
                Log.e(TAG, "Failed to compile vertex shader")
                return 0
            }

            val fragmentShaderId = compileShader(FRAGMENT_SHADER, GLES20.GL_FRAGMENT_SHADER)
            if (fragmentShaderId == 0) {
                GLES20.glDeleteShader(vertexShaderId)
                Log.e(TAG, "Failed to compile fragment shader")
                return 0
            }

            // 创建程序
            val program = GLES20.glCreateProgram()
            if (program == 0) {
                Log.e(TAG, "Failed to create program")
                return 0
            }

            // 附加着色器
            GLES20.glAttachShader(program, vertexShaderId)
            GLES20.glAttachShader(program, fragmentShaderId)

            // 绑定属性位置 - 必须在链接之前
            GLES20.glBindAttribLocation(program, 0, "position")
            GLES20.glBindAttribLocation(program, 1, "uv")

            // 链接程序
            GLES20.glLinkProgram(program)

            // 检查链接状态
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                val log = GLES20.glGetProgramInfoLog(program)
                Log.e(TAG, "Failed to link program: $log")
                GLES20.glDeleteProgram(program)
                return 0
            }

            // 删除着色器
            GLES20.glDeleteShader(vertexShaderId)
            GLES20.glDeleteShader(fragmentShaderId)

            // 使用程序
            GLES20.glUseProgram(program)

            return program
        } catch (e: Exception) {
            Log.e(TAG, "Error creating shader program", e)
            return 0
        }
    }

    private fun compileShader(shaderCode: String, shaderType: Int): Int {
        // 创建着色器
        val shaderId = GLES20.glCreateShader(shaderType)
        if (shaderId == 0) {
            Log.e(TAG, "Failed to create shader")
            return 0
        }

        // 加载着色器源码
        GLES20.glShaderSource(shaderId, shaderCode)
        GLES20.glCompileShader(shaderId)

        // 检查编译状态
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shaderId)
            Log.e(TAG, "Shader compilation failed: $log")
            GLES20.glDeleteShader(shaderId)
            return 0
        }

        return shaderId
    }

    fun dispose() {
        if (programId != 0) {
            GLES20.glDeleteProgram(programId)
            programId = 0
        }
    }
} 