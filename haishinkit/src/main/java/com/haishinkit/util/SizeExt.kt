package com.haishinkit.util

import android.util.Size

internal fun Size.swap(swapped: Boolean): Size {
    return if (swapped) { Size(height, width) } else { this }
}

internal val Size.aspectRatio: Float
    get() = width.toFloat() / height.toFloat()
