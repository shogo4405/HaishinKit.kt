package com.haishinkit.media

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import com.haishinkit.media.mediaprojection.MediaCodecSurfaceStrategy
import com.haishinkit.media.mediaprojection.SurfaceStrategy
import com.haishinkit.rtmp.RTMPStream
import com.haishinkit.yuv.ARGB8888toYUV420SemiPlanarConverter
import org.apache.commons.lang3.builder.ToStringBuilder

class MediaProjectionSource(private var mediaProjection: MediaProjection, private val metrics: DisplayMetrics) : VideoSource {
    override var stream: RTMPStream? = null
    override val isRunning: Boolean
        get() = surfaceStrategy.isRunning
    private var byteConverter = ARGB8888toYUV420SemiPlanarConverter()
    private var virtualDisplay: VirtualDisplay? = null
    private var surfaceStrategy: SurfaceStrategy = MediaCodecSurfaceStrategy(metrics)

    override fun setUp() {
        surfaceStrategy.stream = stream
        surfaceStrategy.setUp()
        stream?.videoCodec?.byteConverter = byteConverter
        stream?.videoSetting?.width = (metrics.widthPixels).toInt()
        stream?.videoSetting?.height = (metrics.heightPixels).toInt()
    }

    override fun tearDown() {
        surfaceStrategy.tearDown()
    }

    override fun startRunning() {
        if (isRunning) return
        surfaceStrategy.startRunning()
        virtualDisplay = mediaProjection.createVirtualDisplay(
            MediaProjectionSource.DEFAULT_DISPLAY_NAME,
            (metrics.widthPixels).toInt(),
            (metrics.heightPixels).toInt(),
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surfaceStrategy.surface,
            null,
            null
        )
    }

    override fun stopRunning() {
        if (!isRunning) return
        virtualDisplay?.release()
        mediaProjection.stop()
        surfaceStrategy.stopRunning()
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val DEFAULT_DISPLAY_NAME = "MediaProjectionSourceDisplay"
    }
}
