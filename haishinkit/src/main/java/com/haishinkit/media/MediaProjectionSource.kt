package com.haishinkit.media

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.Surface
import com.haishinkit.BuildConfig
import com.haishinkit.codec.util.ScheduledFpsController
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.net.NetStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A video source that captures a display by the MediaProjection API.
 */
class MediaProjectionSource(
    private val context: Context,
    private var mediaProjection: MediaProjection,
    private val metrics: DisplayMetrics,
    override val fpsControllerClass: Class<*>? = ScheduledFpsController::class.java,
    override var utilizable: Boolean = false
) :
    VideoSource, PixelTransform.Listener {
    var scale = 0.5F
    var rotatesWithContent = true
    override var stream: NetStream? = null
        set(value) {
            field = value
            stream?.videoCodec?.fpsControllerClass = fpsControllerClass
        }
    override val isRunning = AtomicBoolean(false)
    override var resolution = Size(1, 1)
        set(value) {
            field = value
            stream?.videoSetting?.width = value.width
            stream?.videoSetting?.height = value.height
        }
    private var virtualDisplay: VirtualDisplay? = null

    override fun setUp() {
        if (utilizable) return
        resolution =
            Size((metrics.widthPixels * scale).toInt(), (metrics.heightPixels * scale).toInt())
        stream?.videoCodec?.setAssetManager(context.assets)
        stream?.videoCodec?.setListener(this)
        super.setUp()
    }

    override fun tearDown() {
        if (!utilizable) return
        mediaProjection.stop()
        super.tearDown()
    }

    override fun startRunning() {
        if (isRunning.get()) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "startRunning()")
        }
        isRunning.set(true)
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "stopRunning()")
        }
        virtualDisplay?.release()
        isRunning.set(false)
    }

    override fun onPixelTransformImageAvailable(pixelTransform: PixelTransform) {
    }

    override fun onPixelTransformSurfaceChanged(pixelTransform: PixelTransform, surface: Surface?) {
        pixelTransform.createInputSurface(resolution.width, resolution.height, 0x1)
    }

    override fun onPixelTransformInputSurfaceCreated(pixelTransform: PixelTransform, surface: Surface) {
        var flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
        if (rotatesWithContent) {
            flags += VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT
        }
        virtualDisplay = mediaProjection.createVirtualDisplay(
            DEFAULT_DISPLAY_NAME,
            resolution.width,
            resolution.height,
            metrics.densityDpi,
            flags,
            surface,
            null,
            null
        )
    }

    companion object {
        const val DEFAULT_DISPLAY_NAME = "MediaProjectionSourceDisplay"
        private const val VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT = 128
        private val TAG = MediaProjectionSource::class.java.simpleName
    }
}
