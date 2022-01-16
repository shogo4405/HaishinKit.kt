package com.haishinkit.graphics

import javax.microedition.khronos.opengles.GL10

enum class ResampleFilter(val rawValue: Int, val glValue: Int) {
    LINEAR(0, GL10.GL_LINEAR),
    NEAREST(1, GL10.GL_NEAREST),
    CUBIC(2, GL10.GL_LINEAR)
}
