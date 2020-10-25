package com.haishinkit.gles.renderer

import android.util.Size
import com.haishinkit.gles.GlPixelContext
import com.haishinkit.util.VideoGravity

internal class GlNullPixelRenderer(
    override var utilizable: Boolean = false,
    override var resolution: Size = Size(0, 0),
    override var videoGravity: Int = VideoGravity.RESIZE_ASPECT_FILL
) : GlPixelRenderer {
    override fun setUp() {
    }

    override fun tearDown() {
    }

    override fun render(context: GlPixelContext, matrix: FloatArray) {
    }

    companion object {
        var instance = GlNullPixelRenderer()
    }
}
