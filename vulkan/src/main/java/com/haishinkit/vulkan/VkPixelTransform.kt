package com.haishinkit.vulkan

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import android.util.Size
import android.view.Surface
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.effect.DefaultVideoEffect
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.screen.Screen
import java.nio.ByteBuffer

class VkPixelTransform(override val applicationContext: Context) : PixelTransform {
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
        nativeSetVideoEffect(DefaultVideoEffect.shared)
    }

    override var screen: Screen? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
        }

    override var surface: Surface? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetSurface(value)
        }

    override var imageExtent = Size(0, 0)
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetImageExtent(imageExtent.width, imageExtent.height)
        }

    override var videoGravity: VideoGravity = VideoGravity.RESIZE_ASPECT_FILL
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetVideoGravity(field.rawValue)
        }

    override var videoEffect: VideoEffect = DefaultVideoEffect.shared
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetVideoEffect(videoEffect)
        }

    override var frameRate: Int = 60
        set(value) {
            if (field == value) {
                return
            }
            field = value
            nativeSetFrameRate(frameRate)
        }

    @Suppress("UNUSED")
    private var memory: Long = 0

    external fun inspectDevices(): String

    private external fun nativeIsSupported(): Boolean

    private external fun nativeSetImageOrientation(imageOrientation: Int)

    private external fun nativeSetSurface(surface: Surface?)

    private external fun nativeSetDeviceOrientation(surfaceRotation: Int)

    private external fun nativeSetResampleFilter(resampleFilter: Int)

    private external fun nativeSetVideoGravity(videoGravity: Int)

    private external fun nativeSetImageExtent(
        width: Int,
        height: Int
    )

    private external fun nativeSetAssetManager(assetManager: AssetManager?)

    private external fun nativeCreateInputSurface(
        width: Int,
        height: Int,
        format: Int
    ): Surface?

    private external fun nativeSetRotatesWithContent(expectedOrientationSynchronize: Boolean)

    private external fun nativeSetFrameRate(frameRate: Int)

    private external fun nativeSetVideoEffect(videoEffect: VideoEffect)

    private external fun nativeReadPixels(buffer: ByteBuffer)

    private external fun nativeDispose()
}
