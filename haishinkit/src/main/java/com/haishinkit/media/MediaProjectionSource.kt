package com.haishinkit.media

import android.annotation.TargetApi
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import com.haishinkit.codec.H264Encoder
import com.haishinkit.rtmp.RTMPStream
import com.haishinkit.yuv.ARGB8888toYUV420SemiPlanarConverter
import org.apache.commons.lang3.builder.ToStringBuilder


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class MediaProjectionSource(private var mediaProjection: MediaProjection, private val metrics: DisplayMetrics): IVideoSource, ImageReader.OnImageAvailableListener {
    override var stream: RTMPStream? = null
    override var isRunning: Boolean = false
    private var virtualDisplay: VirtualDisplay? = null

    override fun setUp() {
        val width = (metrics.widthPixels).toInt()
        val height = (metrics.heightPixels).toInt()
        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2).also {
            it.setOnImageAvailableListener(this, null)
        }
        if (reader != null) {
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                    MediaProjectionSource.DEFAULT_DISPLAY_NAME,
                    width,
                    height,
                    metrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    reader.surface,
                    null,
                    null
            )
        }
        val encoder = stream?.getEncoderByName("video/avc") as? H264Encoder
        val byteConverter = ARGB8888toYUV420SemiPlanarConverter()
        byteConverter.width = width
        byteConverter.height = height
        encoder?.width = width
        encoder?.height = height
        encoder?.byteConverter = byteConverter
    }

    override fun tearDown() {
        kotlin.runCatching {
            virtualDisplay?.release()
            mediaProjection.stop()
        }
    }

    override fun startRunning() {
        isRunning = true
    }

    override fun stopRunning() {
        isRunning = false
    }

    override fun onImageAvailable(reader: ImageReader) {
        reader.acquireLatestImage().use { img ->
            kotlin.runCatching {
                val plane = img?.planes?.get(0) ?: return@use null
                val width = plane.rowStride / plane.pixelStride
                val height = (metrics.heightPixels).toInt()
                val encoder = stream?.getEncoderByName("video/avc") as? H264Encoder
                val converter = encoder?.byteConverter as? ARGB8888toYUV420SemiPlanarConverter
                converter?.width = width
                converter?.height = height
                val byteArray = ByteArray(plane.buffer.remaining()).apply {
                    plane.buffer.get(this)
                }
                stream?.appendBytes(byteArray, System.nanoTime() / 1000000L, RTMPStream.BufferType.VIDEO)
            }.exceptionOrNull()?.also {
                Log.w(javaClass.name, "", it)
            }
        }
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val DEFAULT_DISPLAY_NAME = "MediaProjectionSourceDisplay"
    }
}
