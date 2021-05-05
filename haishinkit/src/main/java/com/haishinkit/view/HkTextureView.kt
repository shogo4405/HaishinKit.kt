package com.haishinkit.view

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Size
import android.view.Surface
import android.view.TextureView
import com.haishinkit.event.Event
import com.haishinkit.event.EventUtils
import com.haishinkit.event.IEventDispatcher
import com.haishinkit.event.IEventListener
import com.haishinkit.net.NetStream
import com.haishinkit.rtmp.RtmpStream
import com.haishinkit.util.MediaFormatUtil
import com.haishinkit.util.VideoGravity
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean

class HkTextureView(context: Context, attributes: AttributeSet) : TextureView(context, attributes), NetStreamView, IEventListener, TextureView.SurfaceTextureListener {
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    override var videoGravity: Int = VideoGravity.RESIZE_ASPECT_FILL
    override var stream: NetStream? = null
        set(value) {
            (field as? IEventDispatcher)?.removeEventListener(Event.RTMP_STATUS, this)
            (value as? IEventDispatcher)?.addEventListener(Event.RTMP_STATUS, this)
            field = value
        }
    private var resolution = Size(0, 0)

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
        surfaceTexture?.let {
            stream?.surface = Surface(it)
        }
        isRunning.set(true)
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        isRunning.set(false)
    }

    override fun handleEvent(event: Event) {
        val data = EventUtils.toMap(event)
        when (data["code"].toString()) {
            RtmpStream.Code.VIDEO_DIMENSION_CHANGE.rawValue -> {
                stream?.videoCodec?.outputFormat?.let { format ->
                    val width = MediaFormatUtil.getWidth(format)
                    val height = MediaFormatUtil.getHeight(format)
                    resolution = Size(width, height)
                    surfaceTexture?.let { surface ->
                        onSurfaceTextureSizeChanged(surface, this.width, this.height)
                    }
                }
            }
            else -> {
            }
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        stream?.let {
            it.surface = Surface(surface)
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        val degrees = if (resolution.width < resolution.height) {
            // portrait
            if (width < height) {
                0f
            } else {
                90f
            }
        } else {
            // landscape
            if (width < height) {
                90f
            } else {
                0f
            }
        }
        val matrix = Matrix()
        val src = RectF(0f, 0f, resolution.width.toFloat(), resolution.height.toFloat())
        val dst = RectF(0f, 0f, this.width.toFloat(), this.height.toFloat())
        val screen = RectF(dst)
        matrix.postRotate(degrees, screen.centerX(), screen.centerY())
        matrix.mapRect(dst)
        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER)
        matrix.mapRect(src)
        matrix.setRectToRect(screen, src, Matrix.ScaleToFit.FILL)
        matrix.postRotate(degrees, screen.centerX(), screen.centerY())
        setTransform(matrix)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        private val TAG = HkTextureView::class.java.simpleName
    }
}
