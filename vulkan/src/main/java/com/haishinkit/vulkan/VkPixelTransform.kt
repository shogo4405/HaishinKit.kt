package com.haishinkit.vulkan

import android.content.res.AssetManager
import android.util.Log
import android.util.Size
import android.view.Surface
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.ResampleFilter
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.filter.DefaultVideoEffect
import com.haishinkit.graphics.filter.VideoEffect

class VkPixelTransform(override var listener: PixelTransform.Listener? = null) : PixelTransform {
    companion object {
        init {
            System.loadLibrary("hkvulkan")
        }

        /**
         * A Boolean value indicating whether the current device supports the Vulkan API.
         */
        external fun isSupported(): Boolean

        private const val TAG = "VkPixelTransform"
    }

    init {
        if (nativeIsSupported()) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, inspectDevices())
            }
        } else {
            throw UnsupportedOperationException()
        }
    }

    override var outputSurface: Surface? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetSurface(value)
        }

    override var imageOrientation: ImageOrientation = ImageOrientation.UP
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetImageOrientation(imageOrientation.rawValue)
        }

    override var imageExtent = Size(0, 0)
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetImageExtent(imageExtent.width, imageExtent.height)
        }

    override var fpsControllerClass: Class<*>? = null

    override var expectedOrientationSynchronize = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetExpectedOrientationSynchronize(value)
        }

    override var videoGravity: VideoGravity = VideoGravity.RESIZE_ASPECT_FILL
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetVideoGravity(field.rawValue)
        }

    override var resampleFilter: ResampleFilter = ResampleFilter.CUBIC
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetResampleFilter(value.rawValue)
        }

    override var assetManager: AssetManager? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetAssetManager(value)
        }

    override var surfaceRotation: Int = Surface.ROTATION_0
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetSurfaceRotation(value)
        }

    override var videoEffect: VideoEffect = DefaultVideoEffect()

    @Suppress("unused")
    private var memory: Long = 0

    external fun inspectDevices(): String

    override fun createInputSurface(width: Int, height: Int, format: Int) {
        val surface = nativeCreateInputSurface(width, height, format)
        listener?.onPixelTransformInputSurfaceCreated(this, surface)
    }

    override fun dispose() {
        nativeDispose()
    }

    private external fun nativeIsSupported(): Boolean
    private external fun nativeSetImageOrientation(imageOrientation: Int)
    private external fun nativeSetSurface(surface: Surface?)
    private external fun nativeSetSurfaceRotation(surfaceRotation: Int)
    private external fun nativeSetResampleFilter(resampleFilter: Int)
    private external fun nativeSetVideoGravity(videoGravity: Int)
    private external fun nativeSetImageExtent(width: Int, height: Int)
    private external fun nativeSetAssetManager(assetManager: AssetManager?)
    private external fun nativeCreateInputSurface(width: Int, height: Int, format: Int): Surface
    private external fun nativeSetExpectedOrientationSynchronize(expectedOrientationSynchronize: Boolean)
    private external fun nativeDispose()
}
