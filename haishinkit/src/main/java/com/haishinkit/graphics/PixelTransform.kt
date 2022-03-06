package com.haishinkit.graphics

import android.content.res.AssetManager
import android.util.Size
import android.view.Surface
import com.haishinkit.graphics.filter.VideoEffect

interface PixelTransform {
    interface Listener {
        fun onPixelTransformImageAvailable(pixelTransform: PixelTransform)
        fun onPixelTransformSurfaceChanged(pixelTransform: PixelTransform, surface: Surface?)
        fun onPixelTransformInputSurfaceCreated(pixelTransform: PixelTransform, surface: Surface)
    }

    var surface: Surface?
    var listener: Listener?

    /**
     * The current width and height of the surface
     */
    var extent: Size
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
