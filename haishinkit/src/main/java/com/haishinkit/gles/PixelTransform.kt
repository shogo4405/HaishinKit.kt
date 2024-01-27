package com.haishinkit.gles

import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.GLES20
import android.util.Log
import android.util.Size
import android.view.Choreographer
import android.view.Surface
import com.haishinkit.graphics.FpsController
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.ScheduledFpsController
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.effect.DefaultVideoEffect
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.lang.Running
import com.haishinkit.screen.Screen
import com.haishinkit.screen.Video
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

internal class PixelTransform : PixelTransform, Running, Choreographer.FrameCallback {
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    override var screen: Screen? = null
        set(value) {
            field = value
            if (value == null) {
                stopRunning()
            } else {
                startRunning()
            }
        }
    override var surface: Surface? = null
        set(value) {
            field = value
            if (value == null) {
                stopRunning()
            } else {
                startRunning()
            }
        }

    override var videoGravity: VideoGravity
        get() {
            return video.videoGravity
        }
        set(value) {
            video.videoGravity = value
        }

    override var imageExtent = Size(0, 0)
        set(value) {
            field = value
            GLES20.glViewport(
                0, 0, imageExtent.width, imageExtent.height
            )
            video.frame.set(0, 0, imageExtent.width, imageExtent.height)
            video.invalidateLayout()
        }

    override var videoEffect: VideoEffect = DefaultVideoEffect.shared
        set(value) {
            field = value
            program = shaderLoader.createProgram(GLES20.GL_TEXTURE_2D, videoEffect)
        }

    override var frameRate: Int
        get() = fpsController.frameRate
        set(value) {
            fpsController.frameRate = value
        }

    private val shaderLoader by lazy {
        val shaderLoader = ShaderLoader()
        shaderLoader.assetManager = screen?.assetManager
        shaderLoader
    }
    private val context: Context by lazy { Context() }
    private var choreographer: Choreographer? = null
        set(value) {
            field?.removeFrameCallback(this)
            field = value
            field?.postFrameCallback(this)
        }
    private var program: Program? = null
        set(value) {
            field?.dispose()
            field = value
        }

    private val video: Video by lazy { Video(target = GLES20.GL_TEXTURE_2D) }
    private val fpsController: FpsController by lazy { ScheduledFpsController() }
    private val screenRenderer: PixelTransformRenderer by lazy { PixelTransformRenderer() }

    override fun startRunning() {
        if (isRunning.get()) return
        if (screen == null || surface == null) return
        isRunning.set(true)
        video.videoGravity = VideoGravity.RESIZE_ASPECT
        fpsController.clear()
        context.apply {
            open((screen as? com.haishinkit.gles.screen.ThreadScreen)?.context)
            makeCurrent(createWindowSurface(surface))
        }
        program = shaderLoader.createProgram(GLES20.GL_TEXTURE_2D, videoEffect)
        screen?.let {
            video.videoSize = it.frame.size
        }
        choreographer = Choreographer.getInstance()
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        program = null
        choreographer = null
        isRunning.set(false)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (isRunning.get()) {
            choreographer?.postFrameCallback(this)
        }
        val screen = screen ?: return
        var timestamp = frameTimeNanos
        if (timestamp <= 0L && !fpsController.advanced(timestamp)) return
        timestamp = fpsController.timestamp(timestamp)
        if (surface == null) {
            return
        }
        try {
            GLES20.glClearColor(0f, 0f, 0f, 0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            if (video.videoSize.width != screen.frame.size.width || video.videoSize.height != screen.frame.size.height) {
                video.videoSize = Size(screen.frame.size.width, screen.frame.size.height)
            }
            if (video.shouldInvalidateLayout) {
                video.id = screen.id
                video.layout(screenRenderer)
            }
            program?.draw(video)
            context.setPresentationTime(timestamp)
            context.swapBuffers()
        } catch (e: RuntimeException) {
            Log.e(TAG, "", e)
        }

    }

    override fun readPixels(lambda: (bitmap: Bitmap?) -> Unit) {
        if (surface == null) {
            lambda(null)
            return
        }
        val bitmap =
            Bitmap.createBitmap(imageExtent.width, imageExtent.height, Bitmap.Config.ARGB_8888)
        val byteBuffer =
            ByteBuffer.allocateDirect(imageExtent.width * imageExtent.height * 4).apply {
                order(ByteOrder.LITTLE_ENDIAN)
            }
        context.readPixels(imageExtent.width, imageExtent.height, byteBuffer)
        bitmap.copyPixelsFromBuffer(byteBuffer)
        lambda(
            Bitmap.createBitmap(
                bitmap, 0, 0, imageExtent.width, imageExtent.height, Matrix().apply {
                    setRotate(
                        180.0F
                    )
                }, false
            )
        )
    }

    companion object {
        private val TAG = PixelTransform::class.java.simpleName
    }
}
