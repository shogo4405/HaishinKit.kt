package com.haishinkit.graphics

import android.content.res.AssetManager
import android.os.Handler
import android.util.Size
import android.view.Choreographer
import android.view.Surface
import com.haishinkit.graphics.util.FpsController
import com.haishinkit.graphics.util.ScheduledFpsController
import com.haishinkit.graphics.filter.VideoEffect
import com.haishinkit.graphics.gles.GlKernel
import com.haishinkit.graphics.gles.GlTexture

internal class GlPixelTransform(
    override var listener: PixelTransform.Listener? = null,
) : PixelTransform, Choreographer.FrameCallback {
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

    override var expectedOrientationSynchronize: Boolean
        get() = kernel.expectedOrientationSynchronize
        set(value) {
            kernel.expectedOrientationSynchronize = value
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
    private val kernel: GlKernel by lazy {
        GlKernel()
    }
    private val fpsController: FpsController by lazy {
        ScheduledFpsController()
    }
    private var texture: GlTexture? = null
        set(value) {
            field?.release()
            field = value
        }
    private var running = false

    override fun createInputSurface(width: Int, height: Int, format: Int) {
        if (texture != null && texture?.isValid(width, height) == true) {
            texture?.surface?.let {
                listener?.onPixelTransformInputSurfaceCreated(this, it)
            }
            return
        }
        texture = GlTexture.create(width, height).apply {
            surface?.let {
                listener?.onPixelTransformInputSurfaceCreated(this@GlPixelTransform, it)
            }
        }
        choreographer = Choreographer.getInstance()
        if (outputSurface != null) {
            startRunning()
        }
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
                kernel.render(it.id, it.extent, timestamp)
            }
        }
    }

    override fun dispose() {
        running = false
        texture = null
        listener = null
        kernel.tearDown()
    }

    private fun startRunning() {
        if (running) {
            return
        }
        running = true
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
