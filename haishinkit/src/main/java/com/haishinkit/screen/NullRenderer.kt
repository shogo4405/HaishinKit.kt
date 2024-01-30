package com.haishinkit.screen

internal class NullRenderer : Renderer {
    override fun layout(screenObject: ScreenObject) {
    }

    override fun draw(screenObject: ScreenObject) {
    }

    override fun bind(screenObject: ScreenObject) {
    }

    override fun unbind(screenObject: ScreenObject) {
    }

    companion object {
        internal val SHARED = NullRenderer()
    }
}
