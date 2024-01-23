package com.haishinkit.gles.screen

import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.Rect
import android.opengl.GLES20
import android.view.Choreographer
import com.haishinkit.gles.Context
import com.haishinkit.gles.Framebuffer
import com.haishinkit.gles.Utils
import com.haishinkit.lang.Running
import com.haishinkit.screen.ScreenObject
import java.util.concurrent.atomic.AtomicBoolean

internal class Screen : com.haishinkit.screen.Screen(), Running, Choreographer.FrameCallback {
    val context: Context by lazy { Context() }
    override var id: Int
        get() = framebuffer.textureId
        set(value) {
        }

    override var bounds: Rect
        get() = super.bounds
        set(value) {
            super.bounds = value
            framebuffer.bounds = value
        }

    override var deviceOrientation: Int
        get() = screenRenderer.deviceOrientation
        set(value) {
            screenRenderer.deviceOrientation = value
        }

    override var assetManager: AssetManager?
        get() = screenRenderer.assetManager
        set(value) {
            screenRenderer.assetManager = value
        }

    override val isRunning: AtomicBoolean = AtomicBoolean(false)

    private var choreographer: Choreographer? = null
        set(value) {
            field?.removeFrameCallback(this)
            field = value
            field?.postFrameCallback(this)
        }
    private val screenRenderer: ScreenRenderer by lazy { ScreenRenderer() }
    private val framebuffer: Framebuffer by lazy { Framebuffer() }

    override fun addChild(child: ScreenObject) {
        super.addChild(child)
        screenRenderer.bind(child)
    }

    override fun removeChild(child: ScreenObject) {
        super.removeChild(child)
        screenRenderer.unbind(child)
    }

    override fun dispose() {
        stopRunning()
        super.dispose()
    }

    override fun startRunning() {
        if (isRunning.get()) return
        isRunning.set(true)
        context.open(null)
        context.makeCurrent(null)
        choreographer = Choreographer.getInstance()
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        isRunning.set(false)
        choreographer = null
        framebuffer.release()
        context.close()
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (isRunning.get()) {
            choreographer?.postFrameCallback(this)
        }

        if (!framebuffer.isEnabled) return

        layout(screenRenderer)
        screenRenderer.shouldInvalidateLayout = false

        framebuffer.render {
            GLES20.glClearColor(
                (Color.red(backgroundColor) / 255).toFloat(),
                (Color.green(backgroundColor) / 255).toFloat(),
                (Color.blue(backgroundColor) / 255).toFloat(),
                0f
            )
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            draw(screenRenderer)
            GLES20.glDisable(GLES20.GL_BLEND)
        }

        GLES20.glFlush()
        Utils.checkGlError("glFlush")
    }

    companion object {
        private val TAG = Screen::class.java.simpleName
    }
}