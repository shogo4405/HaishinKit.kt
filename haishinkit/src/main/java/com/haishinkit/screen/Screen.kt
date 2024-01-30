package com.haishinkit.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color

/**
 * An object that manages offscreen rendering a foundation.
 */
abstract class Screen(val applicationContext: Context) : ScreenObjectContainer() {
    abstract class Callback {
        abstract fun onEnterFrame()
    }

    /**
     * Specifies the screen's background color.
     */
    open var backgroundColor: Int = Color.BLACK
    protected var callbacks = mutableListOf<Callback>()

    /**
     * Reads the pixels of a displayed image.
     */
    abstract fun readPixels(lambda: ((bitmap: Bitmap?) -> Unit))

    /**
     * Binds the gpu texture.
     */
    abstract fun bind(screenObject: ScreenObject)

    /***
     * Unbinds the gpu texture.
     */
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
     * Unregisters a screen listener.
     */
    open fun unregisterCallback(callback: Callback) {
        if (callbacks.contains(callback)) {
            callbacks.remove(callback)
        }
    }

    companion object {
        fun create(context: Context): Screen {
            return com.haishinkit.gles.screen.ThreadScreen(context)
        }
    }
}
