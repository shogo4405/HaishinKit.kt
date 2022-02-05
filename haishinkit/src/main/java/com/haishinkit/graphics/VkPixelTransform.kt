package com.haishinkit.graphics

import android.content.res.AssetManager
import android.graphics.ImageFormat
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import java.nio.ByteBuffer

class VkPixelTransform(override var listener: PixelTransform.Listener? = null) :
    PixelTransform,
    ImageReader.OnImageAvailableListener {
    companion object {
        init {
            System.loadLibrary("haishinkit")
        }

        /**
         * A Boolean value indicating whether the current device supports the Vulkan API.
         */
        external fun isSupported(): Boolean

        private const val MAX_IMAGES = 2
        private const val TAG = "VkPixelTransform"
    }

    override var surface: Surface? = null
        set(value) {
            field = value
            nativeSetSurface(value)
        }

    override var imageOrientation: ImageOrientation = ImageOrientation.UP
        set(value) {
            field = value
            nativeSetImageOrientation(imageOrientation.rawValue)
        }

    override var extent: Size = Size(0, 0)

    override var fpsControllerClass: Class<*>? = null

    override var videoGravity: VideoGravity = VideoGravity.RESIZE_ASPECT_FILL
        set(value) {
            field = value
            nativeSetVideoGravity(field.rawValue)
        }

    override var resampleFilter: ResampleFilter = ResampleFilter.CUBIC
        set(value) {
            field = value
            nativeSetResampleFilter(field.rawValue)
        }

    override var assetManager: AssetManager? = null
        set(value) {
            field = value
            nativeSetAssetManager(assetManager)
        }

    override var surfaceOrientation: Int = Surface.ROTATION_0

    @Suppress("unused")
    private var memory: Long = 0
    private lateinit var reader: ImageReader
    private var buffer: ByteBuffer? = null
    private var isMultiPlanar: Boolean = false
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

    external fun inspectDevices(): String

    override fun createInputSurface(width: Int, height: Int, format: Int) {
        isMultiPlanar = when (format) {
            ImageFormat.YUV_420_888 -> true
            else -> false
        }
        buffer = null
        reader = ImageReader.newInstance(width, height, format, MAX_IMAGES)
        setTexture(width, height, format)
        reader.setOnImageAvailableListener(this, handler)
        listener?.onPixelTransformInputSurfaceCreated(this, reader.surface)
    }

    override fun onImageAvailable(reader: ImageReader) {
        val image = reader.acquireNextImage()
        try {
            if (image.planes.size == 1) {
                updateTexture(
                    image.planes[0].buffer,
                    null,
                    null,
                    image.planes[0].rowStride,
                    0,
                    0
                )
            } else {
                updateTexture(
                    image.planes[0].buffer,
                    image.planes[1].buffer,
                    image.planes[2].buffer,
                    image.planes[0].rowStride,
                    image.planes[1].rowStride,
                    image.planes[1].pixelStride
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "", e)
        }
        image.close()
    }

    protected fun finalize() {
        dispose()
    }

    private external fun nativeSetImageOrientation(imageOrientation: Int)
    private external fun nativeSetSurface(surface: Surface?)
    private external fun nativeSetResampleFilter(resampleFilter: Int)
    private external fun nativeSetVideoGravity(videoGravity: Int)
    private external fun nativeSetAssetManager(assetManager: AssetManager?)
    private external fun setTexture(width: Int, height: Int, format: Int)
    private external fun updateTexture(
        buffer0: ByteBuffer? = null,
        buffer1: ByteBuffer? = null,
        buffer2: ByteBuffer? = null,
        buffer0Stride: Int,
        buffer1Stride: Int,
        uvPixelStride: Int
    )

    private external fun dispose()
}
