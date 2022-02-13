package com.haishinkit.graphics

import android.content.res.AssetManager
import android.util.Size
import android.view.Surface

interface PixelTransform {
    interface Listener {
        fun onPixelTransformSetUp(pixelTransform: PixelTransform)
        fun onPixelTransformInputSurfaceCreated(pixelTransform: PixelTransform, surface: Surface)
    }

    var surface: Surface?
    var listener: Listener?

    /**
     * The current width and height of the surface
     */
    var extent: Size
    var resampleFilter: ResampleFilter
    var imageOrientation: ImageOrientation
    var surfaceRotation: Int
    var videoGravity: VideoGravity
    var assetManager: AssetManager?
    var fpsControllerClass: Class<*>?

    fun createInputSurface(width: Int, height: Int, format: Int)
}
