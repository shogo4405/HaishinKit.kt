package com.haishinkit.gles.screen

import android.content.res.AssetManager
import android.graphics.Rect
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import com.haishinkit.gles.Context
import com.haishinkit.screen.Screen
import com.haishinkit.screen.ScreenObject
import java.lang.ref.WeakReference

internal class ThreadScreen : Screen() {
    val context: Context
        get() {
            return screen.context
        }

    override var id: Int
        get() = screen.id
        set(value) {
        }

    override var bounds: Rect
        get() = screen.bounds
        set(value) {
            handler.apply {
                sendMessage(obtainMessage(MSG_SET_BOUNDS, value))
            }
        }

    override var assetManager: AssetManager?
        get() = screen.assetManager
        set(value) {
            handler.apply {
                sendMessage(obtainMessage(MSG_SET_ASSET_MANAGER, value))
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

    private val screen: com.haishinkit.gles.screen.Screen by lazy { com.haishinkit.gles.screen.Screen() }

    private val handler: Handler by lazy {
        val thread = HandlerThread(TAG)
        thread.start()
        Handler(screen, thread.looper)
    }

    init {
        handler.apply {
            sendMessage(obtainMessage(MSG_START_RUNNING))
        }
    }

    override fun addChild(child: ScreenObject) {
        handler.apply {
            sendMessage(obtainMessage(MSG_ADD_CHILD, child))
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
                    transform.bounds = message.obj as Rect
                }

                MSG_SET_ASSET_MANAGER -> {
                    if (message.obj == null) {
                        transform.assetManager = null
                    } else {
                        transform.assetManager = message.obj as AssetManager
                    }
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
        private const val MSG_SET_ASSET_MANAGER = 1
        private const val MSG_SET_BACKGROUND_COLOR = 2
        private const val MSG_SET_DEVICE_ORIENTATION = 3
        private const val MSG_START_RUNNING = 4
        private const val MSG_ADD_CHILD = 5
        private const val MSG_REMOVE_CHILD = 6
        private const val MSG_DISPOSE = 7
    }
}
