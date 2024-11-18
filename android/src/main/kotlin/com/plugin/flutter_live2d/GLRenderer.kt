package com.plugin.flutter_live2d

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        LAppDelegate.getInstance().onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        LAppDelegate.getInstance().onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        LAppDelegate.getInstance().run()
    }
} 