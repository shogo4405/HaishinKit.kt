package com.haishinkit.graphics

import android.content.res.AssetManager
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.view.Surface
import java.lang.ref.WeakReference

class GlThreadPixelTransform(
    override var surface: Surface? = null,
    override var inputSurface: Surface? = null,
    override var assetManager: AssetManager? = null,
    override var fpsControllerClass: Class<*>? = null,
) : PixelTransform, PixelTransform.Listener {
    override var listener: PixelTransform.Listener? = null
    private var handler: Handler? = null
        get() {
            if (field == null) {
                val thread = HandlerThread(TAG)
                thread.start()
                field = Handler(pixelTransform, thread.looper)
                pixelTransform.handler = field
            }
            return field
        }
        set(value) {
            field?.looper?.quitSafely()
            field = value
        }
    private val pixelTransform: GlPixelTransform by lazy {
        val pixelTransform = GlPixelTransform()
        pixelTransform.listener = this
        pixelTransform
    }

    override fun setUp(surface: Surface?, width: Int, height: Int) {
        handler?.let {
            it.sendMessage(it.obtainMessage(MSG_SET_UP, width, height, surface))
        }
    }

    override fun createInputSurface(width: Int, height: Int, format: Int) {
        handler?.let {
            it.sendMessage(it.obtainMessage(MSG_CREATE_INPUT_SURFACE, width, height, format))
        }
    }

    private class Handler(frame: GlPixelTransform, looper: Looper) : android.os.Handler(looper) {
        private val weakTransform: WeakReference<GlPixelTransform> =
            WeakReference<GlPixelTransform>(frame)

        override fun handleMessage(message: Message) {
            val transform = weakTransform.get() ?: return
            when (message.what) {
                MSG_SET_UP -> {
                    val obj = message.obj
                    transform.setUp(obj as Surface, message.arg1, message.arg2)
                }
                MSG_CREATE_INPUT_SURFACE -> {
                    val obj = message.obj
                    transform.createInputSurface(message.arg1, message.arg2, obj as Int)
                }
                else ->
                    throw RuntimeException("Unhandled msg what=$message.what")
            }
        }
    }

    override fun onSetUp(pixelTransform: PixelTransform) {
        listener?.onSetUp(this)
    }

    override fun onCreateInputSurface(pixelTransform: PixelTransform, surface: Surface) {
        listener?.onCreateInputSurface(this, surface)
    }

    companion object {
        private const val MSG_SET_UP = 0
        private const val MSG_CREATE_INPUT_SURFACE = 1

        private val TAG = GlThreadPixelTransform::class.java.simpleName
    }
}
