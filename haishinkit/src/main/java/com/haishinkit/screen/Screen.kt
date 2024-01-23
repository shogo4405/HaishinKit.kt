package com.haishinkit.screen

import android.content.res.AssetManager
import android.graphics.Color
import android.view.Surface

abstract class Screen : ScreenObjectContainer() {
    open var assetManager: AssetManager? = null
    open var backgroundColor: Int = BACKGROUND_COLOR
    open var deviceOrientation: Int = Surface.ROTATION_0

    companion object {
        const val BACKGROUND_COLOR = Color.BLACK

        fun create(): Screen {
            return com.haishinkit.gles.screen.ThreadScreen()
        }
    }
}
