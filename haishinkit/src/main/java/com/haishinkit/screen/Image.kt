package com.haishinkit.screen

import android.graphics.Bitmap


/**
 * An Image is a texture mapped onto it.
 */
open class Image : ScreenObject() {
    var bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        set(value) {
            if (field == value) return
            field.recycle()
            field = value
            invalidateLayout()
        }

    override val width: Int
        get() {
            if (frame.size.width == 0) {
                return bitmap.width
            }
            return frame.size.width
        }

    override val height: Int
        get() {
            if (frame.size.height == 0) {
                return bitmap.height
            }
            return frame.size.height
        }

    init {
        matrix[5] = matrix[5] * -1
    }
}
