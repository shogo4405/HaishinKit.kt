package com.haishinkit.graphics

import android.content.res.AssetManager
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.os.Handler
import android.util.Size
import android.view.Surface
import com.haishinkit.codec.util.DefaultFpsController
import com.haishinkit.codec.util.FpsController
import com.haishinkit.graphics.gles.GlKernel

internal class GlPixelTransform(
    override var inputSurface: Surface? = null,
    override var fpsControllerClass: Class<*>? = null,
    override var assetManager: AssetManager? = null,
    override var listener: PixelTransform.Listener? = null,
) : PixelTransform, SurfaceTexture.OnFrameAvailableListener {
    override var surface: Surface?
        get() = kernel.surface
        set(value) {
            kernel.surface = value
            listener?.onSetUp(this)
        }
    override var imageOrientation: ImageOrientation = ImageOrientation.UP
        get() = kernel.imageOrientation
        set(value) {
            field = value
            kernel.imageOrientation = value
        }
    override var videoGravity: VideoGravity = VideoGravity.RESIZE_ASPECT_RESIZE
        get() = kernel.videoGravity
        set(value) {
            field = value
            kernel.videoGravity = value
        }
    override var extent: Size
        get() = kernel.extent
        set(value) {
            kernel.extent = value
        }
    override var surfaceOrientation: Int = Surface.ROTATION_0
        set(value) {
            field = value
        }
    override var resampleFilter: ResampleFilter = ResampleFilter.NEAREST
    var handler: Handler? = null
    private var kernel: GlKernel = GlKernel()
    private var transform = FloatArray(16)
    private var fpsController: FpsController = DefaultFpsController.instance
    private var texture: SurfaceTexture? = null
    private var textureId: Int = 0
    private var textureSize: Size = Size(0, 0)

    override fun createInputSurface(width: Int, height: Int, format: Int) {
        val textures = intArrayOf(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        textureSize = Size(width, height)
        val texture = SurfaceTexture(textures[0])
        texture.setOnFrameAvailableListener(this, handler)
        this.texture = texture
        listener?.onCreateInputSurface(this, Surface(texture))
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        if (surface == null) {
            return
        }
        surfaceTexture.updateTexImage()
        var timestamp = surfaceTexture.timestamp
        if (timestamp <= 0L) {
            return
        }
        if (fpsController.advanced(timestamp)) {
            timestamp = fpsController.timestamp(timestamp)
            surfaceTexture.getTransformMatrix(transform)
            kernel.render(textureId, textureSize, transform)
            // inputWindowSurface.setPresentationTime(timestamp)
        }
    }

    companion object {
        private val TAG = GlPixelTransform::class.java.simpleName
    }
}
