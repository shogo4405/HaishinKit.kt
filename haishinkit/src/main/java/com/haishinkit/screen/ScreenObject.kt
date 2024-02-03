package com.haishinkit.screen

import android.graphics.Point
import android.opengl.GLES20
import com.haishinkit.graphics.effect.DefaultVideoEffect
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.util.EdgeInsets
import com.haishinkit.util.Rectangle
import kotlin.math.max

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
     * Specifies the frame.
     */
    open var frame = Rectangle(Point(0, 0), Rectangle.MATCH_PARENT)
        set(value) {
            if (field == value) return
            field = value
            invalidateLayout()
        }

    /**
     * Specifies the default spacing to laying out content in the screen object.
     */
    val layoutMargins: EdgeInsets = EdgeInsets(0, 0, 0, 0)

    /**
     * The mvp matrix.
     */
    val matrix =
        FloatArray(16).apply {
            this[0] = 1f
            this[5] = 1f
            this[10] = 1f
            this[15] = 1f
        }

    /**
     * Specifies the alignment position along the horizontal axis.
     */
    var horizontalAlignment: Int = HORIZONTAL_ALIGNMENT_LEFT

    /**
     * Specifies the alignment position along the vertical axis.
     */
    var verticalAlignment: Int = VERTICAL_ALIGNMENT_TOP

    /**
     * Specifies the video effect such as a monochrome, a sepia.
     */
    var videoEffect: VideoEffect = DefaultVideoEffect.shared

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
                    parentX + (parentWidth - width) - layoutMargins.right
                }

                else -> {
                    parentX + frame.point.x + layoutMargins.left
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
                    parentY + (parentHeight - height) - layoutMargins.bottom
                }

                else -> {
                    parentY + frame.point.y + layoutMargins.top
                }
            }
        }

    /**
     * The width of the object in pixels.
     */
    open val width: Int
        get() {
            if (frame.size.width == 0) {
                return max(
                    (parent?.frame?.size?.width ?: 0) - layoutMargins.left - layoutMargins.right,
                    0,
                )
            }
            return frame.size.width
        }

    /**
     * The height of the object in pixels.
     */
    open val height: Int
        get() {
            if (frame.size.height == 0) {
                return max(
                    (parent?.frame?.size?.height ?: 0) - layoutMargins.top - layoutMargins.bottom,
                    0,
                )
            }
            return frame.size.height
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
    open fun layout(renderer: Renderer) {
        renderer.layout(this)
        shouldInvalidateLayout = false
    }

    /**
     * Draws the screen object.
     */
    open fun draw(renderer: Renderer) {
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
