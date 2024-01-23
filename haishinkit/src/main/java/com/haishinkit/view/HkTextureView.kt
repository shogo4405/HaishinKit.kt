package com.haishinkit.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.PixelTransformFactory
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.net.NetStream

/**
 * A view that displays a video content of a NetStream object which uses [TextureView].
 */
class HkTextureView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    TextureView(context, attrs, defStyleAttr, defStyleRes),
    NetStreamDrawable,
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

    override var videoEffect: VideoEffect
        get() = pixelTransform.videoEffect
        set(value) {
            pixelTransform.videoEffect = value
        }

    private val pixelTransform: PixelTransform by lazy { PixelTransformFactory().create() }

    private var stream: NetStream? = null
        set(value) {
            field?.drawable = null
            field = value
            field?.drawable = this
            pixelTransform.screen = value?.screen
        }

    init {
        surfaceTextureListener = this
    }

    override fun attachStream(stream: NetStream?) {
        this.stream = stream
    }

    override fun readPixels(lambda: (bitmap: Bitmap?) -> Unit) {
        pixelTransform.readPixels(lambda)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        pixelTransform.imageExtent = Size(width, height)
        pixelTransform.surface = Surface(surface)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        pixelTransform.imageExtent = Size(width, height)
        (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
            ?.defaultDisplay
            ?.orientation
            ?.let { stream?.screen?.deviceOrientation = it }
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        pixelTransform.surface = null
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}


    companion object {
        private val TAG = HkTextureView::class.java.simpleName
    }
}
