package com.haishinkit.media

import android.opengl.GLSurfaceView
import android.util.Size
import com.haishinkit.gles.GlPixelContext
import com.haishinkit.util.VideoGravity
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * An interface that captures a video source.
 */
interface VideoSource : Source {
    interface GlRenderer : GLSurfaceView.Renderer {
        var videoGravity: Int
        var context: GlPixelContext
    }

    class NullRenderer : GlRenderer {
        override var videoGravity = VideoGravity.RESIZE_ASPECT_FILL
        override var context = GlPixelContext.instance

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        }

        override fun onDrawFrame(gl: GL10) {
        }

        companion object {
            internal val instance = NullRenderer()
        }
    }

    var resolution: Size
    val fpsControllerClass: Class<*>?

    fun createGLSurfaceViewRenderer(): GlRenderer? {
        return null
    }
}
