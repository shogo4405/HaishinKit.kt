package com.haishinkit.graphics

import android.content.res.AssetManager
import android.view.Surface

interface PixelTransform {
    interface Listener {
        fun onSetUp(pixelTransform: PixelTransform)
        fun onCreateInputSurface(pixelTransform: PixelTransform, surface: Surface)
    }

    var surface: Surface?
    var listener: Listener?
    var orientation: Int
    var videoGravity: Int
    var inputSurface: Surface?
    var assetManager: AssetManager?
    var fpsControllerClass: Class<*>?

    fun setUp(surface: Surface?, width: Int, height: Int)
    fun createInputSurface(width: Int, height: Int, format: Int)
}
