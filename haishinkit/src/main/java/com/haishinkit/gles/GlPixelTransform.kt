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
import com.haishinkit.codec.util.DefaultFpsController
import com.haishinkit.codec.util.FpsController
import com.haishinkit.gles.renderer.GlFramePixelRenderer
import com.haishinkit.gles.renderer.GlPixelRenderer
import java.lang.ClassCastException
import java.lang.ref.WeakReference

internal class GlPixelTransform {
    interface Listener {
        fun onConfiguration()
    }

    val reader = GlPixelReader()
    var context = GlPixelContext.instance
    var fpsControllerClass: Class<*>? = null
    var pixelRendererClass: Class<*>? = null
    private var listener: Listener? = null
    private var transform = FloatArray(16)
    private var fpsController: FpsController = DefaultFpsController.instance
    private var pixelRenderer: GlPixelRenderer = GlFramePixelRenderer()
    private var inputWindowSurface = GlWindowSurface()
    private var handler: Handler? = null
        get() {
            if (field == null) {
                val thread = HandlerThread(TAG)
                thread.start()
                field = Handler(this, thread.looper)
            }
            return field
        }
        set(value) {
            field?.looper?.quitSafely()
            field = value
        }

    fun setListener(listener: Listener?) {
        handler?.let {
            if (listener == null) {
                it.sendMessage(it.obtainMessage(MSG_SET_LISTENER))
            } else {
                it.sendMessage(it.obtainMessage(MSG_SET_LISTENER, listener))
            }
        }
    }

    fun frameAvailable(surfaceTexture: SurfaceTexture) {
        var timestamp = surfaceTexture.timestamp
        if (timestamp <= 0L) {
            return
        }
        if (fpsController.advanced(timestamp)) {
            timestamp = fpsController.timestamp(timestamp)
            surfaceTexture.getTransformMatrix(transform)
            handler?.let {
                it.sendMessage(it.obtainMessage(MSG_FRAME_AVAILABLE, (timestamp shr 32).toInt(), timestamp.toInt(), transform))
            }
        }
    }

    fun configure(surface: Surface, width: Int, height: Int) {
        handler?.let {
            it.sendMessage(it.obtainMessage(MSG_CONFIGURATION, width, height, surface))
        }
    }

    private fun onSetListener(listener: Listener?) {
        this.listener = listener
    }

    private fun onConfiguration(surface: Surface, width: Int, height: Int) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "configuration for ${width}x$height surface=$surface")
        }
        fpsControllerClass?.let {
            if (fpsController is DefaultFpsController) {
                fpsController = try {
                    it.newInstance() as FpsController
                } catch (e: ClassCastException) {
                    fpsController
                }
                Log.d(TAG, fpsController.toString())
            }
        }
        pixelRendererClass?.let {
            if (pixelRenderer is GlFramePixelRenderer) {
                pixelRenderer = try {
                    it.newInstance() as GlPixelRenderer
                } catch (e: ClassCastException) {
                    pixelRenderer
                }
            }
        }
        reader.setUp(width, height)
        fpsController.clear()
        pixelRenderer.tearDown()
        inputWindowSurface.tearDown()
        inputWindowSurface.setUp(surface, context.eglContext)
        inputWindowSurface.makeCurrent()
        pixelRenderer.resolution = Size(width, height)
        pixelRenderer.setUp()
        GLES10.glOrthof(0.0f, width.toFloat(), height.toFloat(), 0.0f, -1.0f, 1.0f)
        listener?.onConfiguration()
    }

    private fun onFrameAvailable(transform: FloatArray, timestamp: Long) {
        pixelRenderer.render(context, transform)
        inputWindowSurface.setPresentationTime(timestamp)
        if (reader.readable) {
            reader.read(inputWindowSurface, timestamp)
        }
        if (!inputWindowSurface.swapBuffers() && BuildConfig.DEBUG) {
            Log.w(TAG, "can't swap buffers.")
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
                MSG_SET_LISTENER -> {
                    val obj = message.obj
                    if (obj == null) {
                        transform.onSetListener(null)
                    } else {
                        transform.onSetListener(message.obj as Listener)
                    }
                }
                else ->
                    throw RuntimeException("Unhandled msg what=$message.what")
            }
        }
    }

    companion object {
        private const val MSG_CONFIGURATION = 0
        private const val MSG_FRAME_AVAILABLE = 1
        private const val MSG_SET_LISTENER = 2

        private val TAG = GlPixelTransform::class.java.simpleName
    }
}
