package com.haishinkit.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color

abstract class Screen(val applicationContext: Context) : ScreenObjectContainer() {
    abstract class Callback {
        abstract fun onEnterFrame()
    }

    open var backgroundColor: Int = BACKGROUND_COLOR
    protected var callbacks = mutableListOf<Callback>()

    /**
     * Reads the pixels of a displayed image.
     */
    abstract fun readPixels(lambda: ((bitmap: Bitmap?) -> Unit))

    abstract fun bind(screenObject: ScreenObject)

    abstract fun unbind(screenObject: ScreenObject)

    /**
     * Registers a listener to receive notifications about when the Screen.
     */
    open fun registerCallback(callback: Callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback)
        }
    }

    /**
     * Unregisters a Screen listener.
     */
    open fun unregisterCallback(callback: Callback) {
        if (callbacks.contains(callback)) {
            callbacks.remove(callback)
        }
    }

    companion object {
        const val BACKGROUND_COLOR = Color.BLACK

        fun create(context: Context): Screen {
            return com.haishinkit.gles.screen.ThreadScreen(context)
        }
    }
}
