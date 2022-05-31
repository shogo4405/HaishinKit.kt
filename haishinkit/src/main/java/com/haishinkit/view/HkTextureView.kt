package com.haishinkit.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.PixelTransformFactory
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.net.NetStream

class HkTextureView(context: Context, attributes: AttributeSet) :
    TextureView(context, attributes),
    HkView,
    TextureView.SurfaceTextureListener {
    override var videoGravity: VideoGravity
        get() = pixelTransform.videoGravity
        set(value) {
            pixelTransform.videoGravity = value
        }

    override var frameRate: Int
        get() = pixelTransform.frameRate
        set(value) {
            pixelTransform.frameRate = value
        }

    override var imageOrientation: ImageOrientation
        get() = pixelTransform.imageOrientation
        set(value) {
            pixelTransform.imageOrientation = value
        }

    override var videoEffect: VideoEffect
        get() = pixelTransform.videoEffect
        set(value) {
            pixelTransform.videoEffect = value
        }

    override var isRotatesWithContent: Boolean
        get() = pixelTransform.isRotatesWithContent
        set(value) {
            pixelTransform.isRotatesWithContent = value
        }

    private val pixelTransform: PixelTransform by lazy {
        PixelTransformFactory().create()
    }

    private var stream: NetStream? = null
        set(value) {
            field?.renderer = null
            field = value
            field?.renderer = this
        }

    init {
        pixelTransform.assetManager = context.assets
        surfaceTextureListener = this
    }

    override fun attachStream(stream: NetStream?) {
        this.stream = stream
    }

    override fun readPixels(lambda: (bitmap: Bitmap?) -> Unit) {
        pixelTransform.readPixels(lambda)
    }

    override fun createInputSurface(
        width: Int,
        height: Int,
        format: Int,
        lambda: ((surface: Surface) -> Unit)
    ) {
        pixelTransform.createInputSurface(width, height, format, lambda)
    }

    override fun dispose() {
        pixelTransform.dispose()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        pixelTransform.imageExtent = Size(width, height)
        pixelTransform.outputSurface = Surface(surface)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        pixelTransform.imageExtent = Size(width, height)
        (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.orientation?.let {
            pixelTransform.deviceOrientation = it
            stream?.videoCodec?.pixelTransform?.deviceOrientation = it
        }
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        pixelTransform.outputSurface = null
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    companion object {
        private val TAG = HkTextureView::class.java.simpleName
    }
}
