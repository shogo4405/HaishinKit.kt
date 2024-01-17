package com.haishinkit.graphics

import android.graphics.Bitmap
import android.util.Size
import android.view.Surface
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.screen.Screen

/**
 * The PixelTransform interface provides some graphics operations.
 */
interface PixelTransform {
    /**
     * Specifies the off screen object.
     */
    var screen: Screen?

    /**
     * Specifies the surface that is an output source.
     */
    var surface: Surface?

    /**
     * Specifies the current width and height of the output surface.
     */
    var imageExtent: Size

    /**
     * Specifies the videoEffect such as a monochrome, a sepia.
     */
    var videoEffect: VideoEffect

    /**
     * Specifies the videoGravity how the displays the inputSurface's visual content.
     */
    var videoGravity: VideoGravity

    /**
     * Specifies the frameRate for an output source in frames/sec.
     */
    var frameRate: Int

    /**
     * Reads the pixels of a displayed image.
     */
    fun readPixels(lambda: ((bitmap: Bitmap?) -> Unit))
}
