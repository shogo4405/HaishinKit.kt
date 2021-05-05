package com.haishinkit.view

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
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

class HkSurfaceView(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes), NetStreamView, IEventListener {
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    override var videoGravity: Int = VideoGravity.RESIZE_ASPECT_FILL
    override var stream: NetStream? = null
        set(value) {
            (field as? IEventDispatcher)?.removeEventListener(Event.RTMP_STATUS, this)
            (value as? IEventDispatcher)?.addEventListener(Event.RTMP_STATUS, this)
            field = value
        }
    private var videoAspectRatio = 0f
        set(value) {
            field = value
            requestLayout()
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
        stream?.surface = holder.surface
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = measuredWidth
        var height = measuredHeight
        if (videoAspectRatio != 0f) {
            val viewAspectRatio = width.toFloat() / height
            val aspectDeformation = videoAspectRatio / viewAspectRatio - 1
            if (aspectDeformation > MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
                height = (width / videoAspectRatio).toInt()
            } else if (aspectDeformation < -MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
                width = (height * videoAspectRatio).toInt()
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        private const val MAX_ASPECT_RATIO_DEFORMATION_PERCENT = 0.01f
        private var TAG = HkSurfaceView::class.java.simpleName
    }
}
