package com.haishinkit.graphics

import android.content.res.AssetManager
import android.util.Size
import android.view.Surface
import com.haishinkit.graphics.filter.VideoEffect

/**
 * The PixelTransform interface provides some graphics operations.
 */
interface PixelTransform {
    interface Listener {
        fun onPixelTransformInputSurfaceCreated(pixelTransform: PixelTransform, surface: Surface)
    }

    /**
     * Specifies the surface that is an output source.
     */
    var outputSurface: Surface?

    /**
     * Specifies the listener on which callback methods.
     */
    var listener: Listener?

    /**
     * Specifies the current width and height of the output surface.
     */
    var imageExtent: Size

    /**
     * Specifies the videoEffect such as a monochrome, a sepia.
     */
    var videoEffect: VideoEffect

    /**
     * Specifies the resampleFilter that is effective on a magFilter and a minFilter for a texture.
     */
    var resampleFilter: ResampleFilter
    var imageOrientation: ImageOrientation
    var surfaceRotation: Int
    var videoGravity: VideoGravity

    /**
     * Specifies the assetManager instance from a context.
     */
    var assetManager: AssetManager?
    var fpsControllerClass: Class<*>?
    var expectedOrientationSynchronize: Boolean

    fun createInputSurface(width: Int, height: Int, format: Int)

    /**
     * Disposes the pixelTransform for memory management.
     */
    fun dispose()
}
