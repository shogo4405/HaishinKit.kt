package com.haishinkit.screen

/**
 *  A ScreenObjectContainer represents a collection of screen objects.
 */
open class ScreenObjectContainer : ScreenObject() {
    private val children = mutableListOf<ScreenObject>()

    val childCounts: Int
        get() = children.size

    /**
     * Adds the specified screen object as a child of the current screen object container.
     */
    open fun addChild(child: ScreenObject) {
        if (child.parent != null) {
            throw IllegalStateException()
        }
        children.add(child)
        child.parent = this
    }

    /**
     * Removes the specified screen object as a child of the current screen object container.
     */
    open fun removeChild(child: ScreenObject) {
        if (child.parent != this) {
            return
        }
        children.remove(child)
        child.parent = null
    }

    override fun layout(renderer: ScreenRenderer) {
        children.forEach {
            if (it.shouldInvalidateLayout || renderer.shouldInvalidateLayout) {
                it.layout(renderer)
            }
        }
    }

    override fun draw(renderer: ScreenRenderer) {
        children.forEach {
            if (it.isVisible) {
                it.draw(renderer)
            }
        }
    }

    open fun dispose() {
        for (i in children.size - 1 downTo 0) {
            removeChild(children[i])
        }
        children.clear()
    }
}
