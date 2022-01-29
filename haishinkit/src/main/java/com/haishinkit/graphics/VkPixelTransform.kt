package com.haishinkit.graphics

import android.content.res.AssetManager
import android.graphics.ImageFormat
import android.media.ImageReader
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
    }

    override var surface: Surface? = null
        set(value) {
            field = value
            nativeSetSurface(value)
        }

    override var inputSurface: Surface? = null
        set(value) {
            field = value
            nativeSetInputSurface(value)
        }

    override var orientation: Int = 0

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

    @Suppress("unused")
    private var memory: Long = 0
    private lateinit var reader: ImageReader
    private var buffer: ByteBuffer? = null
    private var isMultiPlanar: Boolean = false

    external fun inspectDevices(): String

    override fun setUp(surface: Surface?, width: Int, height: Int) {
        this.surface = surface
        listener?.onSetUp(this)
    }

    override fun createInputSurface(width: Int, height: Int, format: Int) {
        isMultiPlanar = when (format) {
            ImageFormat.YUV_420_888 -> true
            else -> false
        }
        buffer = null
        reader = ImageReader.newInstance(width, height, format, MAX_IMAGES)
        setTexture(width, height, format)
        reader.setOnImageAvailableListener(this, null)
        listener?.onCreateInputSurface(this, reader.surface)
    }

    override fun onImageAvailable(reader: ImageReader) {
        val image = reader.acquireNextImage()
        if (isMultiPlanar) {
            if (buffer == null) {
                buffer = ByteBuffer.allocateDirect(
                    image.planes[0].buffer.remaining() +
                        image.planes[1].buffer.remaining() +
                        image.planes[2].buffer.remaining()
                )
            }
            buffer?.apply {
                position(0)
                put(image.planes[0].buffer)
                put(image.planes[1].buffer)
                put(image.planes[2].buffer)
                updateTexture(this, image.width)
            }
        } else {
            updateTexture(image.planes[0].buffer, image.planes[0].rowStride)
        }
        image.close()
    }

    protected fun finalize() {
        dispose()
    }

    private external fun nativeSetSurface(surface: Surface?)
    private external fun nativeSetInputSurface(surface: Surface?)
    private external fun nativeSetResampleFilter(resampleFilter: Int)
    private external fun nativeSetVideoGravity(videoGravity: Int)
    private external fun nativeSetAssetManager(assetManager: AssetManager?)
    private external fun setTexture(width: Int, height: Int, format: Int)
    private external fun updateTexture(buffer: ByteBuffer? = null, stride: Int)
    private external fun dispose()
}
