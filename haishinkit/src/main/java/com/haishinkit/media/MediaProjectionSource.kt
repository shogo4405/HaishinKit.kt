package com.haishinkit.media

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.Choreographer
import android.view.Surface
import com.haishinkit.codec.MediaCodec
import com.haishinkit.codec.util.ScheduledFpsController
import com.haishinkit.gles.GlPixelContext
import com.haishinkit.gles.GlPixelTransform
import com.haishinkit.net.NetStream
import org.apache.commons.lang3.builder.ToStringBuilder
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A video source that captures a display by the MediaProjection API.
 */
class MediaProjectionSource(
    private val context: Context,
    private var mediaProjection: MediaProjection,
    private val metrics: DisplayMetrics,
    override val fpsControllerClass: Class<*>? = ScheduledFpsController::class.java
) :
    VideoSource, Choreographer.FrameCallback, GlPixelTransform.Listener {
    var scale = 0.5F
    override var stream: NetStream? = null
        set(value) {
            field = value
            stream?.videoCodec?.fpsControllerClass = fpsControllerClass
            stream?.videoCodec?.callback = MediaCodec.Callback()
        }
    override val isRunning = AtomicBoolean(false)
    override var resolution = Size(1, 1)
        set(value) {
            field = value
            stream?.videoSetting?.width = value.width
            stream?.videoSetting?.height = value.height
        }
    private var virtualDisplay: VirtualDisplay? = null
    private lateinit var choreographer: Choreographer
    private var surface: Surface? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var pixelContext = GlPixelContext(context, false)

    override fun setUp() {
        stream?.videoCodec?.context = pixelContext
        resolution = Size((metrics.widthPixels * scale).toInt(), (metrics.heightPixels * scale).toInt())
        stream?.videoCodec?.setListener(this)
    }

    override fun tearDown() {
        mediaProjection.stop()
    }

    override fun startRunning() {
        if (isRunning.get()) return
        isRunning.set(true)
    }

    override fun stopRunning() {
        if (!isRunning.get()) return

        choreographer.removeFrameCallback(this)
        pixelContext.tearDown()
        virtualDisplay?.release()

        isRunning.set(false)
    }

    override fun onConfiguration() {
        pixelContext.textureSize = resolution

        pixelContext.setUp()
        surfaceTexture = pixelContext.createSurfaceTexture(resolution.width, resolution.height)
        surface = Surface(surfaceTexture)

        virtualDisplay = mediaProjection.createVirtualDisplay(
            MediaProjectionSource.DEFAULT_DISPLAY_NAME,
            resolution.width,
            resolution.height,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface,
            null,
            null
        )

        choreographer = Choreographer.getInstance()
        choreographer.postFrameCallback(this)
    }

    override fun doFrame(timestamp: Long) {
        try {
            surfaceTexture?.let {
                it.updateTexImage()
                stream?.videoCodec?.frameAvailable(it)
            }
        } catch (e: Exception) {
            Log.d(TAG, "", e)
        }
        choreographer.postFrameCallback(this)
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val DEFAULT_DISPLAY_NAME = "MediaProjectionSourceDisplay"

        private val TAG = MediaProjectionSource::class.java.simpleName
    }
}
