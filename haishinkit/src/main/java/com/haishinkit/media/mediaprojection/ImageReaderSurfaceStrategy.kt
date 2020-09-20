package com.haishinkit.media.mediaprojection

import android.annotation.TargetApi
import android.graphics.PixelFormat
import android.media.ImageReader
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import com.haishinkit.codec.BufferInfo
import com.haishinkit.codec.BufferType
import com.haishinkit.rtmp.RTMPStream

@TargetApi(Build.VERSION_CODES.KITKAT)
internal class ImageReaderSurfaceStrategy(override val metrics: DisplayMetrics) : ISurfaceStrategy, ImageReader.OnImageAvailableListener {
    override var isRunning: Boolean = false
    override var surface: Surface? = null
        get() = reader?.surface
    override var stream: RTMPStream? = null
    private var width: Int = ImageReaderSurfaceStrategy.DEFAULT_WIDTH
    private var height: Int = ImageReaderSurfaceStrategy.DEFAULT_HEIGHT
    private var reader: ImageReader? = null
        set(value) {
            reader?.close()
            field = value
        }

    override fun setUp() {
        width = (metrics.widthPixels).toInt()
        height = (metrics.heightPixels).toInt()
        reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2).also {
            it.setOnImageAvailableListener(this, null)
        }
    }

    override fun tearDown() {
        width = DEFAULT_WIDTH
        height = DEFAULT_HEIGHT
        reader = null
        stream = null
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
                val byteArray = ByteArray(plane.buffer.remaining()).apply {
                    plane.buffer.get(this)
                }
                stream?.appendBytes(
                    byteArray,
                    BufferInfo(
                        type = BufferType.VIDEO,
                        presentationTimeUs = System.nanoTime() / 1000000L,
                        width = width,
                        height = height,
                        rowStride = plane.rowStride,
                        pixelStride = plane.pixelStride
                    )
                )
            }.exceptionOrNull()?.also {
                Log.w(javaClass.name, it)
            }
        }
    }

    companion object {
        private const val DEFAULT_WIDTH: Int = -1
        private const val DEFAULT_HEIGHT: Int = -1
    }
}
