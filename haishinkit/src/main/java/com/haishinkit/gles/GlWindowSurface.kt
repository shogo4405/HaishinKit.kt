package com.haishinkit.gles

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLExt
import android.util.Log
import android.view.Surface
import com.haishinkit.lang.Utilize

internal class GlWindowSurface(
    override var utilizable: Boolean = false
) : Utilize {

    private var display = EGL14.EGL_NO_DISPLAY
    private var context = EGL14.EGL_NO_CONTEXT
    private var surface = EGL14.EGL_NO_SURFACE

    fun makeCurrent(): Boolean {
        if (!utilizable) return false
        if (!EGL14.eglMakeCurrent(display, surface, surface, context)) {
            Log.e(TAG, "eglMakeCurrent failed.")
            return false
        }
        return true
    }

    fun swapBuffers(): Boolean {
        if (!utilizable) return false
        return EGL14.eglSwapBuffers(display, surface)
    }

    fun setPresentationTime(timestamp: Long): Boolean {
        if (!utilizable) return false
        return EGLExt.eglPresentationTimeANDROID(display, surface, timestamp)
    }

    fun setUp(surface: Surface?, eglSharedContext: EGLContext?) {
        if (utilizable) return

        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display === EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException()
        }

        val version = IntArray(2)
        if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
            throw RuntimeException()
        }

        val config = chooseConfig(eglSharedContext) ?: return
        context = EGL14.eglCreateContext(display, config, eglSharedContext ?: EGL14.EGL_NO_CONTEXT, CONTEXT_ATTRIBUTES, 0)
        GlUtil.checkGlError("eglCreateContext")

        this.surface = EGL14.eglCreateWindowSurface(display, config, surface, SURFACE_ATTRIBUTES, 0)
        GlUtil.checkGlError("eglCreateWindowSurface")

        setUp()
    }

    override fun tearDown() {
        if (!utilizable) return

        EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
        EGL14.eglDestroySurface(display, surface)
        EGL14.eglDestroyContext(display, context)
        EGL14.eglReleaseThread()
        EGL14.eglTerminate(display)
        display = EGL14.EGL_NO_DISPLAY
        context = EGL14.EGL_NO_CONTEXT
        surface = EGL14.EGL_NO_SURFACE

        super.tearDown()
    }

    private fun chooseConfig(eglSharedContext: EGLContext?): EGLConfig? {
        val attributes: IntArray = CONFIG_ATTRIBUTES_WITH_CONTEXT
        val configs: Array<EGLConfig?> = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(display, attributes, 0, configs, 0, configs.size, numConfigs, 0)) {
            Log.w(TAG, "eglCreateContext RGB888+recordable ES2")
            return null
        }
        return configs[0]
    }

    companion object {
        private const val EGL_RECORDABLE_ANDROID: Int = 0x3142

        private val TAG = GlWindowSurface::class.java.simpleName
        private val CONTEXT_ATTRIBUTES = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
        private val SURFACE_ATTRIBUTES = intArrayOf(EGL14.EGL_NONE)

        private val CONFIG_ATTRIBUTES_WITH_CONTEXT = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_NONE
        )
    }
}
