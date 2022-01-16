package com.haishinkit.view

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import com.haishinkit.event.Event
import com.haishinkit.event.EventUtils
import com.haishinkit.event.IEventDispatcher
import com.haishinkit.event.IEventListener
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.PixelTransformFactory
import com.haishinkit.net.NetStream
import com.haishinkit.rtmp.RtmpStream
import com.haishinkit.util.MediaFormatUtil
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.gles.GlKernel.Companion.ROTATION_0
import com.haishinkit.graphics.gles.GlKernel.Companion.ROTATION_180
import com.haishinkit.graphics.gles.GlKernel.Companion.ROTATION_270
import com.haishinkit.graphics.gles.GlKernel.Companion.ROTATION_90
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("unused")
class HkSurfaceView(context: Context, attributes: AttributeSet) :
    SurfaceView(context, attributes),
    NetStreamView,
    IEventListener {
    var videoOrientation: Int = Surface.ROTATION_0
        set(value) {
            field = value
            pixelTransform.orientation = field
            stream?.videoCodec?.pixelTransform?.orientation = field
        }
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    override var videoGravity: Int = VideoGravity.RESIZE_ASPECT_FILL
    override var stream: NetStream? = null
        set(value) {
            (field as? IEventDispatcher)?.removeEventListener(Event.RTMP_STATUS, this)
            (value as? IEventDispatcher)?.addEventListener(Event.RTMP_STATUS, this)
            field = value
            field?.renderer = this
        }
    override val pixelTransform: PixelTransform by lazy {
        PixelTransformFactory().create(true)
    }
    private var isPortrait = false
    private var videoAspectRatio = 0f
        set(value) {
            field = value
            requestLayout()
        }

    init {
        pixelTransform.assetManager = context.assets

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val defaultDisplay = windowManager.defaultDisplay
                val orientation = defaultDisplay.orientation
                isPortrait = if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    (orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_180)
                } else {
                    (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270)
                }
                videoOrientation = when (defaultDisplay.orientation) {
                    Surface.ROTATION_0 -> if (isPortrait) ROTATION_270 else ROTATION_0
                    Surface.ROTATION_90 -> if (isPortrait) ROTATION_0 else ROTATION_90
                    Surface.ROTATION_180 -> if (isPortrait) ROTATION_90 else ROTATION_180
                    Surface.ROTATION_270 -> if (isPortrait) ROTATION_180 else ROTATION_270
                    else -> 0
                }
                pixelTransform.setUp(holder.surface, width, height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
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

    override fun handleEvent(event: Event) {
        val data = EventUtils.toMap(event)
        when (data["code"].toString()) {
            RtmpStream.Code.VIDEO_DIMENSION_CHANGE.rawValue -> {
                stream?.videoCodec?.outputFormat?.let {
                    val width = MediaFormatUtil.getWidth(it)
                    val height = MediaFormatUtil.getHeight(it)
                    this.post {
                        videoAspectRatio = width.toFloat() / height.toFloat()
                    }
                }
            }
            else -> {
            }
        }
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        private const val MAX_ASPECT_RATIO_DEFORMATION_PERCENT = 0.01f
        private var TAG = HkSurfaceView::class.java.simpleName
    }
}
