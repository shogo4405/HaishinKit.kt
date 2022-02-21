package com.haishinkit.view

import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.PixelTransformFactory
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.net.NetStream
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused")
class HkSurfaceView(context: Context, attributes: AttributeSet) :
    SurfaceView(context, attributes),
    HkView {
    var videoOrientation: ImageOrientation = ImageOrientation.UP
        set(value) {
            field = value
            pixelTransform.imageOrientation = field
            stream?.videoCodec?.pixelTransform?.imageOrientation = field
        }
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    override var videoGravity: VideoGravity = VideoGravity.RESIZE_ASPECT_FILL
        set(value) {
            field = value
            pixelTransform.videoGravity = field
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
    private var isPortrait = false

    init {
        pixelTransform.assetManager = context.assets

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                pixelTransform.extent = Size(width, height)
                pixelTransform.surface = holder.surface
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.orientation?.let {
                    pixelTransform.surfaceRotation = it
                }
                pixelTransform.extent = Size(width, height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                pixelTransform.surface = null
            }
        })
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
        stream?.video?.startRunning()
        isRunning.set(true)
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        stream?.video?.stopRunning()
        isRunning.set(false)
    }

    companion object {
        private var TAG = HkSurfaceView::class.java.simpleName
    }
}
