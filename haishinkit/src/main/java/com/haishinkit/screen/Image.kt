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
}
