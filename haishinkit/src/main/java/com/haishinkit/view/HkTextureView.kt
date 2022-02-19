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
import java.util.concurrent.atomic.AtomicBoolean

class HkTextureView(context: Context, attributes: AttributeSet) :
    TextureView(context, attributes),
    HkView,
    TextureView.SurfaceTextureListener {
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    override var videoGravity: VideoGravity = VideoGravity.RESIZE_ASPECT_FILL
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
        surfaceTextureListener = this
    }

    override fun attachStream(stream: NetStream?) {
        this.stream = stream
        if (stream != null) {
            startRunning()
        } else {
            stopRunning()
        }
    }

    override fun startRunning() {
        if (isRunning.get()) return
        isRunning.set(true)
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        isRunning.set(false)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        pixelTransform.extent = Size(width, height)
        pixelTransform.surface = Surface(surface)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.orientation?.let {
            pixelTransform.surfaceRotation = it
            stream?.videoCodec?.pixelTransform?.surfaceRotation = it
        }
        pixelTransform.extent = Size(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        pixelTransform.surface = null
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    companion object {
        private val TAG = HkTextureView::class.java.simpleName
    }
}
