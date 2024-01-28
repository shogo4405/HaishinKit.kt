package com.haishinkit.gles.screen

import android.content.Context
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import com.haishinkit.gles.GraphicsContext
import com.haishinkit.screen.Screen
import com.haishinkit.screen.ScreenObject
import com.haishinkit.util.Rectangle
import java.lang.ref.WeakReference

internal class ThreadScreen(applicationContext: Context) : Screen(applicationContext) {
    val graphicsContext: GraphicsContext
        get() {
            return screen.graphicsContext
        }

    override var id: Int
        get() = screen.id
        set(value) {
        }

    override var frame: Rectangle
        get() = screen.frame
        set(value) {
            handler.apply {
                sendMessage(obtainMessage(MSG_SET_BOUNDS, value))
            }
        }

    override var backgroundColor: Int
        get() = screen.backgroundColor
        set(value) {
            handler.apply {
                sendMessage(obtainMessage(MSG_SET_BACKGROUND_COLOR, value))
            }
        }

    override var deviceOrientation: Int
        get() = screen.deviceOrientation
        set(value) {
            handler.apply {
                sendMessage(obtainMessage(MSG_SET_DEVICE_ORIENTATION, value))
            }
        }

    private val screen: com.haishinkit.gles.screen.Screen by lazy {
        com.haishinkit.gles.screen.Screen(
            applicationContext
        )
    }

    private val handler: Handler by lazy {
        val thread = HandlerThread(TAG)
        thread.start()
        Handler(screen, thread.looper)
    }

    init {
        screen.parent = this
        handler.apply {
            sendMessage(obtainMessage(MSG_START_RUNNING))
        }
    }

    override fun bringChildToFront(child: ScreenObject) {
        handler.apply {
            sendMessage(obtainMessage(MSG_BRING_CHILD_TO_FRONT, child))
        }
    }

    override fun sendChildToBack(child: ScreenObject) {
        handler.apply {
            sendMessage(obtainMessage(MSG_SEND_CHILD_TO_BACK, child))
        }
    }

    override fun addChild(child: ScreenObject) {
        handler.apply {
            sendMessage(obtainMessage(MSG_ADD_CHILD, child))
        }
    }

    override fun bind(screenObject: ScreenObject) {
        handler.apply {
            sendMessage(obtainMessage(MSG_BIND, screenObject))
        }
    }

    override fun unbind(screenObject: ScreenObject) {
        handler.apply {
            sendMessage(obtainMessage(MSG_UNBIND, screenObject))
        }
    }

    override fun registerCallback(callback: Callback) {
        handler.apply {
            sendMessage(obtainMessage(MSG_REGISTER_CALLBACK, callback))
        }
    }

    override fun unregisterCallback(callback: Callback) {
        handler.apply {
            sendMessage(obtainMessage(MSG_UNREGISTER_CALLBACK, callback))
        }
    }

    override fun removeChild(child: ScreenObject) {
        handler.apply {
            sendMessage(obtainMessage(MSG_REMOVE_CHILD, child))
        }
    }

    override fun dispose() {
        handler.apply {
            sendMessage(obtainMessage(MSG_DISPOSE))
        }
    }

    protected fun finalize() {
        handler.looper.quitSafely()
    }

    private class Handler(frame: com.haishinkit.gles.screen.Screen, looper: Looper) :
        android.os.Handler(looper) {
        private val transform = WeakReference(frame)

        override fun handleMessage(message: Message) {
            val transform = transform.get() ?: return
            when (message.what) {
                MSG_SET_BOUNDS -> {
                    transform.frame = message.obj as Rectangle
                }

                MSG_SET_BACKGROUND_COLOR -> {
                    transform.backgroundColor = message.obj as Int
                }

                MSG_SET_DEVICE_ORIENTATION -> {
                    transform.deviceOrientation = message.obj as Int
                }

                MSG_START_RUNNING -> {
                    transform.startRunning()
                }

                MSG_ADD_CHILD -> {
                    transform.addChild(message.obj as ScreenObject)
                }

                MSG_REMOVE_CHILD -> {
                    transform.removeChild(message.obj as ScreenObject)
                }

                MSG_BRING_CHILD_TO_FRONT -> {
                    transform.bringChildToFront(message.obj as ScreenObject)
                }

                MSG_SEND_CHILD_TO_BACK -> {
                    transform.bringChildToFront(message.obj as ScreenObject)
                }

                MSG_REGISTER_CALLBACK -> {
                    transform.registerCallback(message.obj as Screen.Callback)
                }

                MSG_UNREGISTER_CALLBACK -> {
                    transform.unregisterCallback(message.obj as Screen.Callback)
                }

                MSG_BIND -> {
                    transform.bind(message.obj as ScreenObject)
                }

                MSG_UNBIND -> {
                    transform.unbind(message.obj as ScreenObject)
                }

                MSG_DISPOSE -> {
                    transform.dispose()
                }

                else -> throw RuntimeException("Unhandled msg what=$message.what")
            }
        }
    }

    companion object {
        private val TAG = ThreadScreen::class.java.simpleName

        private const val MSG_SET_BOUNDS = 0
        private const val MSG_SET_BACKGROUND_COLOR = 2
        private const val MSG_SET_DEVICE_ORIENTATION = 3
        private const val MSG_START_RUNNING = 4
        private const val MSG_ADD_CHILD = 5
        private const val MSG_REMOVE_CHILD = 6
        private const val MSG_DISPOSE = 7
        private const val MSG_BRING_CHILD_TO_FRONT = 8
        private const val MSG_SEND_CHILD_TO_BACK = 9
        private const val MSG_REGISTER_CALLBACK = 10
        private const val MSG_UNREGISTER_CALLBACK = 11
        private const val MSG_BIND = 12
        private const val MSG_UNBIND = 13
    }
}
