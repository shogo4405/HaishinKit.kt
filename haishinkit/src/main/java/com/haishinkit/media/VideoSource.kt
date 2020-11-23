package com.haishinkit.media

import android.opengl.GLSurfaceView
import android.util.Size
import com.haishinkit.gles.GlPixelContext

/**
 * An interface that captures a video source.
 */
interface VideoSource : Source {
    interface GlRenderer : GLSurfaceView.Renderer {
        var videoGravity: Int
        var context: GlPixelContext
    }

    var resolution: Size
    val fpsControllerClass: Class<*>?

    fun createGLSurfaceViewRenderer(): GlRenderer? {
        return null
    }
}
