package com.haishinkit.graphics

import android.content.res.AssetManager
import android.media.ImageReader
import android.util.Log
import android.util.Size
import android.view.Surface
import java.nio.ByteBuffer

class VkPixelTransform(override var listener: PixelTransform.Listener? = null) : PixelTransform,
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

    override var surface: Surface?
        external get
        external set

    override var inputSurface: Surface?
        external get
        external set

    override var orientation: Int = 0

    override var extent: Size = Size(0, 0)

    override var fpsControllerClass: Class<*>? = null

    override var videoGravity: VideoGravity = VideoGravity.RESIZE_ASPECT_FILL
        set(value) {
            field = value
            nativeSetVideoGravity(videoGravity.rawValue)
        }

    override var resampleFilter: ResampleFilter = ResampleFilter.LINEAR
        set(value) {
            field = value
            nativeSetResampleFilter(resampleFilter.rawValue)
        }

    override var assetManager: AssetManager? = null
        set(value) {
            field = value
            nativeSetAssetManager(assetManager)
        }

    @Suppress("unused")
    private var memory: Long = 0
    private lateinit var reader: ImageReader

    external fun inspectDevices(): String

    override fun setUp(surface: Surface?, width: Int, height: Int) {
        this.surface = surface
        listener?.onSetUp(this)
    }

    override fun createInputSurface(width: Int, height: Int, format: Int) {
        reader = ImageReader.newInstance(width, height, format, MAX_IMAGES)
        setTexture(width, height, format)
        reader.setOnImageAvailableListener(this, null)
        listener?.onCreateInputSurface(this, reader.surface)
    }

    override fun onImageAvailable(reader: ImageReader) {
        val image = reader.acquireNextImage()
        updateTexture(image.planes[0].buffer, image.planes[0].rowStride)
        image.close()
    }

    protected fun finalize() {
        dispose()
    }

    private external fun nativeSetResampleFilter(resampleFilter: Int)
    private external fun nativeSetVideoGravity(videoGravity: Int)
    private external fun nativeSetAssetManager(assetManager: AssetManager?)
    private external fun setTexture(width: Int, height: Int, format: Int)
    private external fun updateTexture(buffer: ByteBuffer? = null, stride: Int)
    private external fun dispose()
}
