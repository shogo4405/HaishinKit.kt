package com.haishinkit.graphics

import android.content.res.AssetManager
import android.util.Size
import android.view.Surface

interface PixelTransform {
    interface Listener {
        fun onSetUp(pixelTransform: PixelTransform)
        fun onCreateInputSurface(pixelTransform: PixelTransform, surface: Surface)
    }

    var surface: Surface?
    var listener: Listener?

    /**
     * The current width and height of the surface
     */
    var extent: Size
    var resampleFilter: ResampleFilter
    var orientation: Int
    var videoGravity: VideoGravity
    var inputSurface: Surface?
    var assetManager: AssetManager?
    var fpsControllerClass: Class<*>?

    fun setUp(surface: Surface?, width: Int, height: Int)
    fun createInputSurface(width: Int, height: Int, format: Int)
}
