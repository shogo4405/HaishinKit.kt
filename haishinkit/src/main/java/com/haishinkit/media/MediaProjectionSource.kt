package com.haishinkit.media

import android.content.Context
import android.graphics.Rect
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.ChecksSdkIntAtLeast
import com.haishinkit.BuildConfig
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.net.NetStream
import com.haishinkit.screen.Video
import com.haishinkit.util.swap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A video source that captures a display by the MediaProjection API.
 */
@Suppress("UNUSED", "MemberVisibilityCanBePrivate")
class MediaProjectionSource(
    private val context: Context,
    private var mediaProjection: MediaProjection,
    private val metrics: DisplayMetrics,
) : VideoSource, Video.OnSurfaceChangedListener {

    private class Callback : MediaProjection.Callback() {
        override fun onCapturedContentVisibilityChanged(isVisible: Boolean) {
            super.onCapturedContentVisibilityChanged(isVisible)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Callback#onCapturedContentVisibilityChanged:${isVisible}")
            }
        }

        override fun onCapturedContentResize(width: Int, height: Int) {
            super.onCapturedContentResize(width, height)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Callback#onCapturedContentResize:${width}:${height}")
            }
        }

        override fun onStop() {
            super.onStop()
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Callback#onStop")
            }
        }
    }

    /**
     * Specifies scale that defines a transformation that resizes an image.
     */
    var scale = 1.0f

    var isRotatesWithContent = true

    override var stream: NetStream? = null
    override val isRunning = AtomicBoolean(false)
    override val screen: Video by lazy { Video() }
    private var virtualDisplay: VirtualDisplay? = null
        set(value) {
            field?.release()
            field = value
        }
    private var handler: Handler? = null
        get() {
            if (field == null) {
                val thread = HandlerThread(TAG)
                thread.start()
                field = Handler(thread.looper)
            }
            return field
        }
        set(value) {
            field?.looper?.quitSafely()
            field = value
        }

    private var rotation = -1
        set(value) {
            if (value == field) {
                return
            }
            field = value
            screen.imageOrientation = when (value) {
                0 -> ImageOrientation.UP
                1 -> ImageOrientation.LEFT
                2 -> ImageOrientation.DOWN
                3 -> ImageOrientation.RIGHT
                else -> ImageOrientation.UP
            }
        }

    private val orientationEventListener: OrientationEventListener? by lazy {
        if (isAvailableRotatesWithContentFlag) {
            null
        } else {
            object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
                override fun onOrientationChanged(orientation: Int) {
                    val windowManager =
                        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    rotation = windowManager.defaultDisplay.rotation
                    screen.videoSize = if (screen.videoSize.width < screen.videoSize.height) {
                        screen.videoSize.swap(rotation == 1 || rotation == 3)
                    } else {
                        screen.videoSize.swap(rotation == 0 || rotation == 4)
                    }

                }
            }
        }
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
    private var isAvailableRotatesWithContentFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    private val callback: Callback by lazy { Callback() }

    /**
     * Register a listener to receive notifications about when the MediaProjection changes state.
     */
    fun registerCallback(callback: MediaProjection.Callback, handler: Handler?) {
        mediaProjection.registerCallback(callback, handler)
    }

    /**
     * Unregister a MediaProjection listener.
     */
    fun unregisterCallback(callback: MediaProjection.Callback) {
        mediaProjection.unregisterCallback(callback)
    }

    override fun startRunning() {
        if (isRunning.get()) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "startRunning()")
        }
        screen.listener = this
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        stream?.screen?.assetManager = context.assets
        if (isRotatesWithContent) {
            orientationEventListener?.enable()
        }
        // Android 14 must register an callback.
        mediaProjection.registerCallback(callback, null)
        rotation = windowManager.defaultDisplay.rotation
        screen.videoSize =
            Size((metrics.widthPixels * scale).toInt(), (metrics.heightPixels * scale).toInt())
        stream?.screen?.bounds = Rect(0, 0, screen.videoSize.width, screen.videoSize.height)
        stream?.screen?.addChild(screen)
        isRunning.set(true)
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "stopRunning()")
        }
        if (isRotatesWithContent) {
            orientationEventListener?.disable()
        }
        stream?.screen?.removeChild(screen)
        screen.listener = null
        mediaProjection.unregisterCallback(callback)
        mediaProjection.stop()
        virtualDisplay = null
        isRunning.set(false)
    }

    override fun onSurfaceChanged(surface: Surface?) {
        var flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
        if (isAvailableRotatesWithContentFlag) {
            flags += VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT
        }
        handler?.post {
            virtualDisplay = mediaProjection.createVirtualDisplay(
                DEFAULT_DISPLAY_NAME,
                metrics.widthPixels,
                metrics.heightPixels,
                metrics.densityDpi,
                flags,
                surface,
                null,
                handler
            )
        }
    }

    companion object {
        const val DEFAULT_DISPLAY_NAME = "MediaProjectionSourceDisplay"
        private const val VIRTUAL_DISPLAY_FLAG_ROTATES_WITH_CONTENT = 128
        private val TAG = MediaProjectionSource::class.java.simpleName
    }
}
