package com.haishinkit.graphics.gles

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLExt
import android.opengl.GLES20
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer

internal class WindowSurface {

    var config: EGLConfig? = null
    var display = EGL14.EGL_NO_DISPLAY
    var context = EGL14.EGL_NO_CONTEXT
    private var surface = EGL14.EGL_NO_SURFACE

    fun setSurface(surface: Surface?) {
        if (surface == null) {
            EGL14.eglDestroySurface(display, this.surface)
            this.surface = EGL14.EGL_NO_SURFACE
        } else {
            this.surface =
                EGL14.eglCreateWindowSurface(display, config, surface, SURFACE_ATTRIBUTES, 0)
        }
        makeCurrent()
    }

    fun swapBuffers(): Boolean {
        return EGL14.eglSwapBuffers(display, surface)
    }

    fun setPresentationTime(timestamp: Long): Boolean {
        return EGLExt.eglPresentationTimeANDROID(display, surface, timestamp)
    }

    fun readPixels(width: Int, height: Int, buffer: ByteBuffer) {
        buffer.clear()
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)
        buffer.rewind()
    }

    private fun makeCurrent(): Boolean {
        if (!EGL14.eglMakeCurrent(display, surface, surface, context)) {
            Log.e(TAG, "eglMakeCurrent failed.")
            return false
        }
        return true
    }

    companion object {
        private val TAG = WindowSurface::class.java.simpleName
        private val SURFACE_ATTRIBUTES = intArrayOf(EGL14.EGL_NONE)
    }
}
