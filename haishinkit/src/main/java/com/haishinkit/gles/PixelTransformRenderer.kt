package com.haishinkit.gles

import android.view.Surface
import com.haishinkit.screen.ScreenObject
import com.haishinkit.screen.ScreenRenderer

internal class PixelTransformRenderer : ScreenRenderer {
    override var deviceOrientation: Int = Surface.ROTATION_0
        set(value) {
            field = value
            invalidateLayout()
        }
    override var shouldInvalidateLayout: Boolean = true
        private set

    override fun layout(screenObject: ScreenObject) {
        shouldInvalidateLayout = false
    }

    override fun draw(screenObject: ScreenObject) {
    }

    override fun bind(screenObject: ScreenObject) {
    }

    override fun unbind(screenObject: ScreenObject) {
    }

    override fun invalidateLayout() {
        shouldInvalidateLayout = true
    }
}
