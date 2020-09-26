package com.haishinkit.media

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import com.haishinkit.data.VideoResolution
import com.haishinkit.media.mediaprojection.MediaCodecSurfaceStrategy
import com.haishinkit.media.mediaprojection.SurfaceStrategy
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A video source that captures a display by the MediaProjection API.
 */
class MediaProjectionSource(private var mediaProjection: MediaProjection, private val metrics: DisplayMetrics) : VideoSource {
    var scale = 0.5F
    override var stream: RTMPStream? = null
    override val isRunning: AtomicBoolean
        get() = surfaceStrategy.isRunning
    override var resolution: VideoResolution = VideoResolution(1, 1)
        set(value) {
            field = value
            stream?.videoSetting?.width = value.width
            stream?.videoSetting?.height = value.height
        }
    private var virtualDisplay: VirtualDisplay? = null
    private var surfaceStrategy: SurfaceStrategy = MediaCodecSurfaceStrategy(metrics)

    override fun setUp() {
        surfaceStrategy.stream = stream
        surfaceStrategy.setUp()
        resolution = resolution.copy(width = (metrics.widthPixels * scale).toInt(), height = (metrics.heightPixels * scale).toInt())
    }

    override fun tearDown() {
        surfaceStrategy.tearDown()
    }

    override fun startRunning() {
        if (isRunning.get()) return
        virtualDisplay = mediaProjection.createVirtualDisplay(
            MediaProjectionSource.DEFAULT_DISPLAY_NAME,
            resolution.width,
            resolution.height,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surfaceStrategy.surface,
            null,
            null
        )
        surfaceStrategy.startRunning()
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        surfaceStrategy.stopRunning()
        virtualDisplay?.release()
        mediaProjection.stop()
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val DEFAULT_DISPLAY_NAME = "MediaProjectionSourceDisplay"
    }
}
