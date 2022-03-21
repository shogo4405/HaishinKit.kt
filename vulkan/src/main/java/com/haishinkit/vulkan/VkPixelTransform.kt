package com.haishinkit.vulkan

import android.content.res.AssetManager
import android.graphics.ImageFormat
import android.hardware.HardwareBuffer
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.ResampleFilter
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.filter.DefaultVideoEffect
import com.haishinkit.graphics.filter.VideoEffect
import java.nio.ByteBuffer

class VkPixelTransform(override var listener: PixelTransform.Listener? = null) :
    PixelTransform,
    ImageReader.OnImageAvailableListener {
    companion object {
        init {
            System.loadLibrary("libhkvulkan")
        }

        /**
         * A Boolean value indicating whether the current device supports the Vulkan API.
         */
        external fun isSupported(): Boolean

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

    override var expectedOrientationSynchronize: Boolean = false

    override var videoGravity: VideoGravity = VideoGravity.RESIZE_ASPECT_FILL
        set(value) {
            field = value
            nativeSetVideoGravity(field.rawValue)
        }

    override var resampleFilter: ResampleFilter = ResampleFilter.CUBIC
        set(value) {
            field = value
            nativeSetResampleFilter(value.rawValue)
        }

    override var assetManager: AssetManager? = null
        set(value) {
            field = value
            nativeSetAssetManager(value)
        }

    override var surfaceRotation: Int = Surface.ROTATION_0
        set(value) {
            field = value
            nativeSetSurfaceRotation(value)
        }

    override var videoEffect: VideoEffect = DefaultVideoEffect()

    @Suppress("unused")
    private var memory: Long = 0

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
        handler?.post {
            val surface = setTexture(width, height, format)
            listener?.onPixelTransformInputSurfaceCreated(this, surface)
        }
    }

    override fun onImageAvailable(reader: ImageReader) {
    }

    override fun readPixels(byteBuffer: ByteBuffer) {
        nativeReadPixels(byteBuffer)
    }

    override fun dispose() {
        nativeDispose()
    }

    private external fun nativeSetImageOrientation(imageOrientation: Int)
    private external fun nativeSetSurface(surface: Surface?)
    private external fun nativeSetSurfaceRotation(surfaceRotation: Int)
    private external fun nativeSetResampleFilter(resampleFilter: Int)
    private external fun nativeSetVideoGravity(videoGravity: Int)
    private external fun nativeSetAssetManager(assetManager: AssetManager?)
    private external fun setTexture(width: Int, height: Int, format: Int): Surface
    private external fun nativeReadPixels(byteBuffer: ByteBuffer)
    private external fun nativeDispose()
}
