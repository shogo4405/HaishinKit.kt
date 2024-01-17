package com.haishinkit.screen

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Rect

@Suppress("MemberVisibilityCanBePrivate")
class Text : Image() {
    var textValue: String = ""
        set(value) {
            if (field == value) return
            field = value
            invalidateLayout()
        }

    var textColor: Int = Color.WHITE
        set(value) {
            if (field == value) return
            field = value
            invalidateLayout()
        }

    var textSize: Float = 15f
        set(value) {
            if (field == value) return
            field = value
            invalidateLayout()
        }

    private val paint by lazy {
        Paint(ANTI_ALIAS_FLAG).apply {
            textSize = this@Text.textSize
            color = this@Text.textColor
            textAlign = Paint.Align.LEFT
        }
    }

    private var canvas = Canvas(bitmap)
    private var textBounds: Rect = Rect()

    init {
        matrix[5] = matrix[5] * -1
    }

    override fun layout(renderer: ScreenRenderer) {
        paint.getTextBounds(textValue, 0, textValue.length, textBounds)
        bitmap =
            android.graphics.Bitmap.createBitmap(
                textBounds.width(),
                textBounds.height(),
                android.graphics.Bitmap.Config.ARGB_8888
            )
        canvas = Canvas(bitmap)
        bounds.set(bounds.left, bounds.top, bounds.left + bitmap.width, bounds.top + bitmap.height)
        canvas.drawText(textValue, -textBounds.left.toFloat(), -textBounds.top.toFloat(), paint)
        super.layout(renderer)
    }
}
