package com.haishinkit.gles.renderer

import android.util.Size
import com.haishinkit.gles.GlPixelContext
import com.haishinkit.lang.Utilize

interface GlPixelRenderer : Utilize {
    var resolution: Size
    var videoGravity: Int

    fun render(context: GlPixelContext, matrix: FloatArray)
}
