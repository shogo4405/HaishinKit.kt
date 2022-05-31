package com.haishinkit.graphics.gles

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import android.util.Size
import android.view.Choreographer
import android.view.Surface
import com.haishinkit.graphics.FpsController
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.ResampleFilter
import com.haishinkit.graphics.ScheduledFpsController
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.effect.VideoEffect

internal class GlPixelTransform : PixelTransform, Choreographer.FrameCallback {
    override var outputSurface: Surface?
        get() = kernel.outputSurface
        set(value) {
            if (value == null) {
                stopRunning()
            }
            kernel.outputSurface = value
            if (value != null && texture != null) {
                startRunning()
            }
        }

    override var imageOrientation: ImageOrientation
        get() = kernel.imageOrientation
        set(value) {
            kernel.imageOrientation = value
        }

    override var videoGravity: VideoGravity
        get() = kernel.videoGravity
        set(value) {
            kernel.videoGravity = value
        }

    override var videoEffect: VideoEffect
        get() = kernel.videoEffect
        set(value) {
            kernel.videoEffect = value
        }

    override var imageExtent: Size
        get() = kernel.imageExtent
        set(value) {
            kernel.imageExtent = value
        }

    override var deviceOrientation: Int
        get() = kernel.deviceOrientation
        set(value) {
            kernel.deviceOrientation = value
        }

    override var resampleFilter: ResampleFilter
        get() = kernel.resampleFilter
        set(value) {
            kernel.resampleFilter = value
        }

    override var isRotatesWithContent: Boolean
        get() = kernel.isRotatesWithContent
        set(value) {
            kernel.isRotatesWithContent = value
        }

    override var assetManager: AssetManager?
        get() = kernel.assetManager
        set(value) {
            kernel.assetManager = value
        }

    override var frameRate: Int
        get() = fpsController.frameRate
        set(value) {
            fpsController.frameRate = value
        }

    var handler: Handler? = null
        set(value) {
            field?.post { kernel.tearDown() }
            value?.post { kernel.setUp() }
            field = value
        }

    private var choreographer: Choreographer? = null
    private val kernel: Kernel by lazy {
        Kernel()
    }
    private val fpsController: FpsController by lazy {
        ScheduledFpsController()
    }
    private var texture: Texture? = null
        set(value) {
            field?.release()
            field = value
        }
    private var running = false

    override fun createInputSurface(
        width: Int,
        height: Int,
        format: Int,
        lambda: ((surface: Surface) -> Unit)
    ) {
        if (texture != null && texture?.isValid(width, height) == true) {
            texture?.surface?.let {
                lambda(it)
            }
            return
        }
        texture = Texture.create(width, height).apply {
            surface?.let {
                lambda(it)
            }
        }
        choreographer = Choreographer.getInstance()
        if (outputSurface != null) {
            startRunning()
        }
        kernel.invalidateLayout()
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (running) {
            choreographer?.postFrameCallback(this)
        }
        texture?.updateTexImage()
        var timestamp = frameTimeNanos
        if (timestamp <= 0L) {
            return
        }
        if (fpsController.advanced(timestamp)) {
            timestamp = fpsController.timestamp(timestamp)
            if (outputSurface == null) {
                return
            }
            texture?.let {
                try {
                    kernel.render(it.id, it.extent, timestamp)
                } catch (e: RuntimeException) {
                    Log.e(TAG, "", e)
                }
            }
        }
    }

    override fun readPixels(lambda: (bitmap: Bitmap?) -> Unit) {
        lambda(kernel.readPixels())
    }

    override fun dispose() {
        running = false
        texture = null
        kernel.tearDown()
    }

    private fun startRunning() {
        if (running) {
            return
        }
        running = true
        fpsController.clear()
        choreographer?.postFrameCallback(this)
    }

    private fun stopRunning() {
        if (!running) {
            return
        }
        running = false
    }

    companion object {
        private val TAG = GlPixelTransform::class.java.simpleName
    }
}
