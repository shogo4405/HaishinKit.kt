package com.haishinkit.screen

import android.content.Context
import android.graphics.Color
import android.view.Surface

abstract class Screen(val applicationContext: Context) : ScreenObjectContainer() {
    abstract class Callback {
        abstract fun onEnterFrame()
    }

    open var backgroundColor: Int = BACKGROUND_COLOR
    open var deviceOrientation: Int = Surface.ROTATION_0
    protected var callbacks = mutableListOf<Callback>()

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
