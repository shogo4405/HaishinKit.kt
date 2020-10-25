package com.haishinkit.gles

import android.graphics.SurfaceTexture
import android.opengl.GLES10
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.Size
import android.view.Surface
import com.haishinkit.BuildConfig
import com.haishinkit.gles.renderer.GlFramePixelRenderer
import java.lang.ref.WeakReference

internal class GlPixelTransform {
    var context = GlPixelContext.instance

    private var renderer = GlFramePixelRenderer()
    private var transform = FloatArray(16)
    private var inputWindowSurface = GlWindowSurface()
    private var _handler: Handler? = null
    private var handler: Handler?
        get() {
            if (_handler == null) {
                val thread = HandlerThread(TAG)
                thread.start()
                _handler = Handler(this, thread.looper)
            }
            return _handler
        }
        set(value) {
            _handler?.looper?.quitSafely()
            _handler = value
        }

    fun frameAvailable(surfaceTexture: SurfaceTexture) {
        val timestamp = surfaceTexture.timestamp
        if (timestamp <= 0L) {
            return
        }
        surfaceTexture.getTransformMatrix(transform)
        handler?.let {
            it.sendMessage(it.obtainMessage(MSG_FRAME_AVAILABLE, (timestamp shl 32).toInt(), timestamp.toInt(), transform))
        }
    }

    fun configure(surface: Surface, width: Int, height: Int) {
        handler?.let {
            it.sendMessage(it.obtainMessage(MSG_CONFIGURATION, width, height, surface))
        }
    }

    private fun onConfiguration(surface: Surface, width: Int, height: Int) {
        inputWindowSurface.setUp(surface, context.eglContext)
        inputWindowSurface.makeCurrent()
        renderer.resolution = Size(width, height)
        renderer.setUp()
        GLES10.glOrthof(0.0f, width.toFloat(), height.toFloat(), 0.0f, -1.0f, 1.0f)
    }

    private fun onFrameAvailable(transform: FloatArray, timestamp: Long) {
        renderer.render(context, transform)
        // inputWindowSurface.setPresentationTime(timestamp)
        if (!inputWindowSurface.swapBuffers() && BuildConfig.DEBUG) {
            Log.d(TAG, "can't swap buffers.")
        }
    }

    private class Handler(frame: GlPixelTransform?, looper: Looper) : android.os.Handler(looper) {
        private val weakTransform: WeakReference<GlPixelTransform> = WeakReference<GlPixelTransform>(frame)

        override fun handleMessage(message: Message) {
            val transform = weakTransform.get() ?: return
            when (message.what) {
                MSG_CONFIGURATION -> {
                    val obj = message.obj
                    transform.onConfiguration(obj as Surface, message.arg1, message.arg2)
                }
                MSG_FRAME_AVAILABLE -> {
                    val obj = message.obj
                    val timestamp = message.arg1.toLong() shl 32 or (message.arg2.toLong() and 0xffffffffL)
                    transform.onFrameAvailable(obj as FloatArray, timestamp)
                }
                else ->
                    throw RuntimeException("Unhandled msg what=$message.what")
            }
        }
    }

    companion object {
        private const val MSG_CONFIGURATION = 0
        private const val MSG_FRAME_AVAILABLE = 1

        private val TAG = GlPixelTransform::class.java.simpleName
    }
}
