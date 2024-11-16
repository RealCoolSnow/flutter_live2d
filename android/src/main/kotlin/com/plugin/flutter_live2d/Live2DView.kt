package com.plugin.flutter_live2d

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.live2d.sdk.cubism.framework.CubismFramework
import com.live2d.sdk.cubism.framework.motion.CubismMotionQueueManager
import com.live2d.sdk.cubism.framework.model.CubismUserModel
import com.live2d.sdk.cubism.framework.math.CubismMatrix44
import com.live2d.sdk.cubism.framework.rendering.android.CubismOffscreenSurfaceAndroid
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20

class Live2DView(context: Context) : GLSurfaceView(context), GLSurfaceView.Renderer {
    private var model: CubismUserModel? = null
    private var motionManager: CubismMotionQueueManager? = null
    private var modelMatrix: CubismMatrix44? = null
    private var renderingBuffer: CubismOffscreenSurfaceAndroid? = null
    
    private var scale = 1.0f
    private var positionX = 0.0f
    private var positionY = 0.0f

    init {
        // OpenGL ES 2.0を使用する
        setEGLContextClientVersion(2)
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY
        
        // Cubism Framework初期化
        val option = CubismFramework.Option()
        option.logFunction = { message -> println("Live2D: $message") }
        option.loggingLevel = CubismFramework.Option.LogLevel.VERBOSE
        CubismFramework.startUp(option)
        CubismFramework.initialize()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // OpenGL初期化
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        
        // レンダリングバッファの初期化
        renderingBuffer = CubismOffscreenSurfaceAndroid()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        
        // モデル用の行列を定義
        modelMatrix = CubismMatrix44.create()
        modelMatrix?.loadIdentity()
        
        // 画面サイズに合わせてモデルの大きさを調整
        val modelScale = height / 2.0f
        modelMatrix?.scale(modelScale, modelScale)
        
        // モデルの位置を調整
        modelMatrix?.translateX(width / 2.0f)
        modelMatrix?.translateY(height / 2.0f)

        // レンダリングバッファのサイズ設定
        renderingBuffer?.createOffscreenSurface(width, height, null)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Clear buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        renderingBuffer?.beginDraw(null)
        renderingBuffer?.clear(0f, 0f, 0f, 0f)
        
        model?.let { model ->
            // モーション更新
            motionManager?.updateMotion(model, 0.016f)
            
            // モデル行列更新
            modelMatrix?.let { matrix ->
                matrix.loadIdentity()
                matrix.scale(scale, scale)
                matrix.translate(positionX, positionY)
                
                // モデル描画
                model.update()
                model.draw(matrix)
            }
        }
        
        renderingBuffer?.endDraw()
        
        // レンダリング結果を画面に描画
        renderingBuffer?.getColorBuffer(0)?.let { textureId ->
            // ここでテクスチャを画面に描画
            drawTexture(textureId)
        }
    }

    private fun drawTexture(textureId: Int) {
        // テクスチャ描画用のシェーダーとVBOを使用して描画
        // Note: 実際のテクスチャ描画コードをここに実装
    }

    fun loadModel(modelPath: String) {
        // モデル読み込み処理
        // Note: LAppModelのloadAssets()を参考に実装
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                positionX = event.x
                positionY = event.y
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setScale(scale: Float) {
        this.scale = scale
    }

    fun setPosition(x: Float, y: Float) {
        this.positionX = x
        this.positionY = y
    }

    fun startMotion(group: String, index: Int) {
        // モーション開始処理
        // Note: LAppModelのstartMotion()を参考に実装
    }

    fun setExpression(expression: String) {
        // 表情設定処理
        // Note: LAppModelのsetExpression()を参考に実装
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // リソース解放
        renderingBuffer?.destroyOffscreenSurface()
        CubismFramework.dispose()
    }
} 