package com.haishinkit.graphics

import android.content.res.AssetManager
import android.util.Size
import android.view.Surface
import com.haishinkit.graphics.filter.VideoEffect

interface PixelTransform {
    interface Listener {
        fun onPixelTransformInputSurfaceCreated(pixelTransform: PixelTransform, surface: Surface)
    }

    /**
     * The surface that is a input source.
     */
    var surface: Surface?
    var listener: Listener?

    /**
     * The current width and height of the surface
     */
    var imageExtent: Size
    var videoEffect: VideoEffect
    var resampleFilter: ResampleFilter
    var imageOrientation: ImageOrientation
    var surfaceRotation: Int
    var videoGravity: VideoGravity
    var assetManager: AssetManager?
    var fpsControllerClass: Class<*>?
    var expectedOrientationSynchronize: Boolean

    fun createInputSurface(width: Int, height: Int, format: Int)

    /**
     * Disposes the pixelTransform of memory management.
     */
    fun dispose()
}
