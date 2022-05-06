package com.haishinkit.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.PixelTransformFactory
import com.haishinkit.graphics.VideoGravity
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
    override var stream: NetStream? = null
        set(value) {
            field?.renderer = null
            field = value
            field?.renderer = this
        }
    override val pixelTransform: PixelTransform by lazy {
        PixelTransformFactory().create()
    }

    init {
        pixelTransform.assetManager = context.assets
        surfaceTextureListener = this
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
