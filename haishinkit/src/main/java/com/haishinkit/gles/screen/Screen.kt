package com.haishinkit.gles.screen

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20
import android.view.Choreographer
import com.haishinkit.gles.Framebuffer
import com.haishinkit.gles.GraphicsContext
import com.haishinkit.gles.Utils
import com.haishinkit.lang.Running
import com.haishinkit.screen.ScreenObject
import com.haishinkit.util.Rectangle
import java.util.concurrent.atomic.AtomicBoolean

internal class Screen(applicationContext: Context) :
    com.haishinkit.screen.Screen(applicationContext), Running,
    Choreographer.FrameCallback {
    val graphicsContext: GraphicsContext by lazy { GraphicsContext() }
    override var id: Int
        get() = framebuffer.textureId
        set(value) {
        }

    override var frame: Rectangle
        get() = super.frame
        set(value) {
            super.frame = value
            framebuffer.bounds = value
        }

    override var deviceOrientation: Int
        get() = screenRenderer.deviceOrientation
        set(value) {
            screenRenderer.deviceOrientation = value
        }

    override val isRunning: AtomicBoolean = AtomicBoolean(false)

    private var choreographer: Choreographer? = null
        set(value) {
            field?.removeFrameCallback(this)
            field = value
            field?.postFrameCallback(this)
        }
    private val screenRenderer: ScreenRenderer by lazy { ScreenRenderer(applicationContext) }
    private val framebuffer: Framebuffer by lazy { Framebuffer() }

    override fun bind(screenObject: ScreenObject) {
        screenRenderer.bind(screenObject)
    }

    override fun unbind(screenObject: ScreenObject) {
        screenRenderer.unbind(screenObject)
    }

    override fun dispose() {
        stopRunning()
        super.dispose()
    }

    override fun startRunning() {
        if (isRunning.get()) return
        isRunning.set(true)
        graphicsContext.open(null)
        graphicsContext.makeCurrent(null)
        choreographer = Choreographer.getInstance()
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        isRunning.set(false)
        choreographer = null
        framebuffer.release()
        graphicsContext.close()
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (isRunning.get()) {
            for (callback in callbacks) {
                callback.onEnterFrame()
            }
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
