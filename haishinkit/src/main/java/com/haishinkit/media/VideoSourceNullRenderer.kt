package com.haishinkit.media

import com.haishinkit.gles.GlPixelContext
import com.haishinkit.util.VideoGravity
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class VideoSourceNullRenderer : VideoSource.GlRenderer {
    override var videoGravity = VideoGravity.RESIZE_ASPECT_FILL
    override var context = GlPixelContext.instance

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    }

    override fun onDrawFrame(gl: GL10) {
    }

    companion object {
        val instance = VideoSourceNullRenderer()
    }
}
