package com.haishinkit.graphics

import android.content.res.AssetManager
import android.graphics.SurfaceTexture
import android.os.Handler
import android.util.Size
import android.view.Surface
import com.haishinkit.codec.util.DefaultFpsController
import com.haishinkit.codec.util.FpsController
import com.haishinkit.graphics.gles.GlKernel
import com.haishinkit.graphics.gles.GlTexture

internal class GlPixelTransform(
    override var fpsControllerClass: Class<*>? = null,
    override var assetManager: AssetManager? = null,
    override var listener: PixelTransform.Listener? = null,
) : PixelTransform, SurfaceTexture.OnFrameAvailableListener {
    override var surface: Surface?
        get() = kernel.surface
        set(value) {
            kernel.surface = value
            listener?.onPixelTransformSurfaceChanged(this, value)
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
    override var extent: Size
        get() = kernel.extent
        set(value) {
            kernel.extent = value
        }
    override var surfaceRotation: Int
        get() = kernel.surfaceRotation
        set(value) {
            kernel.surfaceRotation = value
        }
    override var resampleFilter: ResampleFilter = ResampleFilter.NEAREST
    override var expectedOrientationSynchronize: Boolean
        get() = kernel.expectedOrientationSynchronize
        set(value) {
            kernel.expectedOrientationSynchronize = value
        }
    var handler: Handler? = null
        set(value) {
            field?.post { kernel.tearDown() }
            value?.post { kernel.setUp() }
            field = value
        }

    private var kernel: GlKernel = GlKernel()
    private var fpsController: FpsController = DefaultFpsController.instance
    private var texture: GlTexture? = null
        set(value) {
            field?.release()
            field = value
        }

    override fun createInputSurface(width: Int, height: Int, format: Int) {
        if (texture != null && texture?.isValid(width, height) == true) {
            texture?.surface?.let {
                listener?.onPixelTransformInputSurfaceCreated(this, it)
            }
            return
        }
        texture = GlTexture.create(width, height).apply {
            setOnFrameAvailableListener(this@GlPixelTransform, handler)
            surface?.let {
                listener?.onPixelTransformInputSurfaceCreated(this@GlPixelTransform, it)
            }
        }
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        if (surface == null) {
            return
        }
        texture?.updateTexImage()
        var timestamp = surfaceTexture.timestamp
        if (timestamp <= 0L) {
            return
        }
        if (fpsController.advanced(timestamp)) {
            timestamp = fpsController.timestamp(timestamp)
            texture?.let {
                kernel.render(it.id, it.extent, timestamp)
            }
        }
    }

    companion object {
        private val TAG = GlPixelTransform::class.java.simpleName
    }
}
