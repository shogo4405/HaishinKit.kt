package com.haishinkit.screen

import android.graphics.Rect
import android.opengl.GLES20

/**
 * The ScreenObject class is the abstract class for all objects that are rendered on the screen.
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class ScreenObject(val target: Int = GLES20.GL_TEXTURE_2D) {
    open var id: Int = -1
        internal set

    /**
     * The screen object container that contains this screen object
     */
    open var parent: ScreenObjectContainer? = null
        internal set(value) {
            (root as? Screen)?.unbind(this)
            field = value
            (root as? Screen)?.bind(this)
        }

    /**
     * Specifies the bounds.
     */
    open var bounds = Rect(0, 0, 0, 0)
        set(value) {
            if (field == value) return
            field = value
            invalidateLayout()
        }

    /**
     * The mvp matrix.
     */
    val matrix = FloatArray(16).apply {
        this[0] = 1f
        this[5] = 1f
        this[10] = 1f
        this[15] = 1f
    }

    var horizontalAlignment: Int = HORIZONTAL_ALIGNMENT_LEFT
    var verticalAlignment: Int = VERTICAL_ALIGNMENT_TOP

    /**
     * The x coordinate.
     */
    val x: Int
        get() {
            val parentX = parent?.x ?: 0
            val parentWidth = parent?.width ?: 0
            return when (horizontalAlignment) {
                HORIZONTAL_ALIGNMENT_CENTER -> {
                    parentX + (parentWidth - width) / 2
                }

                HORIZONTAL_ALIGNMENT_RIGHT -> {
                    parentX + (parentWidth - width)
                }

                else -> {
                    parentX + bounds.top
                }
            }
        }

    /**
     * The y coordinate.
     */
    val y: Int
        get() {
            val parentY = parent?.y ?: 0
            val parentHeight = parent?.height ?: 0
            return when (verticalAlignment) {
                VERTICAL_ALIGNMENT_MIDDLE -> {
                    parentY + (parentHeight - height) / 2
                }

                VERTICAL_ALIGNMENT_BOTTOM -> {
                    parentY + (parentHeight - height)
                }

                else -> {
                    parentY + bounds.left
                }
            }
        }

    /**
     * The width of the object in pixels.
     */
    open val width: Int
        get() {
            if (bounds.width() == 0) {
                return parent?.bounds?.width() ?: 0
            }
            return bounds.width()
        }

    /**
     * The height of the object in pixels.
     */
    open val height: Int
        get() {
            if (bounds.height() == 0) {
                return parent?.bounds?.height() ?: 0
            }
            return bounds.height()
        }

    /**
     * Specifies the visibility of the object.
     */
    var isVisible = true

    var shouldInvalidateLayout = false
        private set

    internal val root: ScreenObject?
        get() {
            var parent: ScreenObject? = this.parent
            while (parent?.parent != null) {
                parent = parent.parent
            }
            return parent
        }

    /**
     * Invalidates the current layout and triggers a layout update.
     */
    fun invalidateLayout() {
        shouldInvalidateLayout = true
        parent?.invalidateLayout()
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

    companion object {
        const val HORIZONTAL_ALIGNMENT_LEFT = 0
        const val HORIZONTAL_ALIGNMENT_CENTER = 1
        const val HORIZONTAL_ALIGNMENT_RIGHT = 2

        const val VERTICAL_ALIGNMENT_TOP = 0
        const val VERTICAL_ALIGNMENT_MIDDLE = 1
        const val VERTICAL_ALIGNMENT_BOTTOM = 2
    }
}
