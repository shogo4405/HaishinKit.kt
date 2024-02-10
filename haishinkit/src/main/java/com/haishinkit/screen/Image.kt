package com.haishinkit.screen

import android.graphics.Bitmap

/**
 * An object that manages offscreen rendering an image source.
 */
open class Image : ScreenObject() {
    var bitmap: Bitmap? = null
        set(value) {
            if (field == value) return
            field?.recycle()
            field = value
            invalidateLayout()
        }

    override val width: Int
        get() {
            if (frame.width() == 0) {
                return bitmap?.width ?: 0
            }
            return frame.width()
        }

    override val height: Int
        get() {
            if (frame.height() == 0) {
                return bitmap?.height ?: 0
            }
            return frame.height()
        }

    init {
        matrix[5] = matrix[5] * -1
    }
}
