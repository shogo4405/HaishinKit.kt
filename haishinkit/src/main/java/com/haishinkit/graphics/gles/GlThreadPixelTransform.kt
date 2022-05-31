package com.haishinkit.graphics.gles

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Size
import android.view.Surface
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.ResampleFilter
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.effect.VideoEffect
import java.lang.ref.WeakReference

internal class GlThreadPixelTransform : PixelTransform {
    override var outputSurface: Surface?
        get() = pixelTransform.outputSurface
        set(value) {
            handler?.let {
                it.sendMessage(it.obtainMessage(MSG_SET_SURFACE, value))
            }
        }
    override var imageOrientation: ImageOrientation
        get() = pixelTransform.imageOrientation
        set(value) {
            handler?.let {
                it.sendMessage(it.obtainMessage(MSG_SET_IMAGE_ORIENTATION, value))
            }
        }
    override var deviceOrientation: Int
        get() = pixelTransform.deviceOrientation
        set(value) {
            handler?.let {
                it.sendMessage(it.obtainMessage(MSG_SET_SURFACE_ORIENTATION, value))
            }
        }
    override var videoGravity: VideoGravity
        get() = pixelTransform.videoGravity
        set(value) {
            handler?.let {
                it.sendMessage(it.obtainMessage(MSG_SET_VIDEO_GRAVITY, value))
            }
        }
    override var videoEffect: VideoEffect
        get() = pixelTransform.videoEffect
        set(value) {
            handler?.let {
                it.sendMessage(it.obtainMessage(MSG_SET_VIDEO_EFFECT, value))
            }
        }
    override var imageExtent: Size
        get() = pixelTransform.imageExtent
        set(value) {
            handler?.let {
                it.sendMessage(
                    it.obtainMessage(
                        MSG_SET_CURRENT_EXTENT,
                        value.width,
                        value.height,
                        null
                    )
                )
            }
        }
    override var resampleFilter: ResampleFilter
        get() = pixelTransform.resampleFilter
        set(value) {
            handler?.let {
                it.sendMessage(
                    it.obtainMessage(MSG_SET_RESAMPLE_FILTER, value)
                )
            }
        }
    override var isRotatesWithContent: Boolean
        get() = pixelTransform.isRotatesWithContent
        set(value) {
            handler?.let {
                it.sendMessage(
                    it.obtainMessage(MSG_SET_EXCEPTED_ORIENTATION_SYNCRONIZE, value)
                )
            }
        }
    override var assetManager: AssetManager?
        get() = pixelTransform.assetManager
        set(value) {
            handler?.let {
                it.sendMessage(
                    it.obtainMessage(MSG_SET_ASSET_MANAGER, value)
                )
            }
        }
    override var frameRate: Int
        get() = pixelTransform.frameRate
        set(value) {
            handler?.let {
                it.sendMessage(
                    it.obtainMessage(MSG_SET_FRAME_RATE, value)
                )
            }
        }
    private var handler: Handler? = null
        get() {
            if (field == null) {
                val thread = HandlerThread(TAG)
                thread.start()
                field = Handler(pixelTransform, thread.looper)
                pixelTransform.handler = field
            }
            return field
        }
        set(value) {
            field?.looper?.quitSafely()
            field = value
        }
    private val pixelTransform: GlPixelTransform by lazy {
        GlPixelTransform()
    }

    override fun readPixels(lambda: (bitmap: Bitmap?) -> Unit) {
        handler?.let {
            it.sendMessage(it.obtainMessage(MSG_READ_PIXELS, lambda))
        }
    }

    override fun createInputSurface(
        width: Int,
        height: Int,
        format: Int,
        lambda: ((surface: Surface) -> Unit)
    ) {
        handler?.let {
            it.sendMessage(it.obtainMessage(MSG_CREATE_INPUT_SURFACE, width, height, lambda))
        }
    }

    override fun dispose() {
        handler?.let {
            it.sendMessage(it.obtainMessage(MSG_DISPOSE))
        }
    }

    private class Handler(frame: GlPixelTransform, looper: Looper) : android.os.Handler(looper) {
        private val weakTransform: WeakReference<GlPixelTransform> =
            WeakReference<GlPixelTransform>(frame)

        override fun handleMessage(message: Message) {
            val transform = weakTransform.get() ?: return
            when (message.what) {
                MSG_SET_SURFACE -> {
                    if (message.obj == null) {
                        transform.outputSurface = null
                    } else {
                        transform.outputSurface = message.obj as Surface
                    }
                }
                MSG_SET_IMAGE_ORIENTATION -> {
                    transform.imageOrientation = message.obj as ImageOrientation
                }
                MSG_SET_SURFACE_ORIENTATION -> {
                    transform.deviceOrientation = message.obj as Int
                }
                MSG_SET_VIDEO_GRAVITY -> {
                    transform.videoGravity = message.obj as VideoGravity
                }
                MSG_SET_CURRENT_EXTENT -> {
                    transform.imageExtent = Size(message.arg1, message.arg2)
                }
                MSG_CREATE_INPUT_SURFACE -> {
                    val obj = message.obj
                    transform.createInputSurface(
                        message.arg1,
                        message.arg2,
                        ImageFormat.PRIVATE,
                        obj as ((surface: Surface) -> Unit)
                    )
                }
                MSG_SET_RESAMPLE_FILTER -> {
                    transform.resampleFilter = message.obj as ResampleFilter
                }
                MSG_SET_EXCEPTED_ORIENTATION_SYNCRONIZE -> {
                    transform.isRotatesWithContent = message.obj as Boolean
                }
                MSG_SET_VIDEO_EFFECT -> {
                    transform.videoEffect = message.obj as VideoEffect
                }
                MSG_SET_ASSET_MANAGER -> {
                    if (message.obj == null) {
                        transform.assetManager = null
                    } else {
                        transform.assetManager = message.obj as AssetManager
                    }
                }
                MSG_SET_FRAME_RATE -> {
                    transform.frameRate = message.obj as Int
                }
                MSG_READ_PIXELS -> {
                    transform.readPixels(message.obj as ((bitmap: Bitmap?) -> Unit))
                }
                MSG_DISPOSE -> {
                    transform.dispose()
                }
                else ->
                    throw RuntimeException("Unhandled msg what=$message.what")
            }
        }
    }

    companion object {
        private const val MSG_SET_SURFACE = 0
        private const val MSG_CREATE_INPUT_SURFACE = 1
        private const val MSG_SET_IMAGE_ORIENTATION = 2
        private const val MSG_SET_SURFACE_ORIENTATION = 3
        private const val MSG_SET_VIDEO_GRAVITY = 4
        private const val MSG_SET_CURRENT_EXTENT = 5
        private const val MSG_SET_RESAMPLE_FILTER = 6
        private const val MSG_SET_EXCEPTED_ORIENTATION_SYNCRONIZE = 7
        private const val MSG_SET_VIDEO_EFFECT = 8
        private const val MSG_SET_ASSET_MANAGER = 9
        private const val MSG_SET_FRAME_RATE = 10
        private const val MSG_READ_PIXELS = 11
        private const val MSG_DISPOSE = 12

        private val TAG = GlThreadPixelTransform::class.java.simpleName
    }
}
