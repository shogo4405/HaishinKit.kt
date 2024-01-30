package com.haishinkit.util

import android.graphics.Point
import android.util.Size

data class Rectangle(var point: Point, var size: Size) {
    fun set(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        point.set(x, y)
        size = Size(width, height)
    }

    companion object {
        val MATCH_PARENT: Size = Size(0, 0)
    }
}
