package com.haishinkit.screen

import android.graphics.Rect
import android.opengl.GLES20

/**
 * The ScreenObject class is the abstract class for all objects that are rendered on the screen.
 */
abstract class ScreenObject(val target: Int = GLES20.GL_TEXTURE_2D) {
    open var id: Int = -1
        internal set

    /**
     * The screen object container that contains this screen object
     */
    var parent: ScreenObjectContainer? = null
        internal set

    open var bounds = Rect(0, 0, 0, 0)
        set(value) {
            if (field == value) return
            field = value
            invalidateLayout()
        }

    val matrix = FloatArray(16).apply {
        this[0] = 1f
        this[5] = 1f
        this[10] = 1f
        this[15] = 1f
    }

    val x: Int
        get() {
            return bounds.left
        }

    val y: Int
        get() {
            return bounds.top
        }

    /**
     * The width of the object in pixels.
     */
    val width: Int
        get() {
            if (bounds.width() == 0 && bounds.height() == 0) {
                return parent?.bounds?.width() ?: bounds.width()
            }
            return bounds.width()
        }

    /**
     * The height of the object in pixels.
     */
    val height: Int
        get() {
            if (bounds.width() == 0 && bounds.height() == 0) {
                return parent?.bounds?.height() ?: bounds.height()
            }
            return bounds.height()
        }

    /**
     * Specifies the visibility of the object.
     */
    var isVisible = true

    var shouldInvalidateLayout = false
        private set

    /**
     * Invalidates the current layout and triggers a layout update.
     */
    fun invalidateLayout() {
        shouldInvalidateLayout = true
    }

    /**
     * Layouts the screen object.
     */
    open fun layout(renderer: ScreenRenderer) {
        renderer.layout(this)
        shouldInvalidateLayout = false
    }

    /**
     * Draws the screen object.
     */
    open fun draw(renderer: ScreenRenderer) {
        renderer.draw(this)
    }
}
