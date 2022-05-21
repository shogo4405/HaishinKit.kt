package com.haishinkit.graphics

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Size
import android.view.Surface
import com.haishinkit.graphics.filter.VideoEffect

/**
 * The PixelTransform interface provides some graphics operations.
 */
interface PixelTransform {
    /**
     * Specifies the surface that is an output source.
     */
    var outputSurface: Surface?

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

    /**
     * Specifies the imageOrientation that describe the image orientation.
     */
    var imageOrientation: ImageOrientation

    /**
     * Specifies the deviceOrientation that describe the physical orientation of the device.
     */
    var deviceOrientation: Int

    /**
     * Specifies the videoGravity how the displays the inputSurface's visual content.
     */
    var videoGravity: VideoGravity

    /**
     * Specifies the assetManager instance from a context.
     */
    var assetManager: AssetManager?

    /**
     * Specifies whether displayed images rotates(true), or not(false).
     */
    var isRotatesWithContent: Boolean

    /**
     * Specifies the frameRate for an output source in frames/sec.
     */
    var frameRate: Int

    /**
     * Reads the pixels of a displayed image.
     */
    fun readPixels(lambda: ((bitmap: Bitmap?) -> Unit))

    fun createInputSurface(
        width: Int,
        height: Int,
        format: Int,
        lambda: ((surface: Surface) -> Unit)
    )

    /**
     * Disposes the pixelTransform for the memory management.
     */
    fun dispose()
}
