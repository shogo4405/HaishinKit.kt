package com.haishinkit.screen

interface Renderer {
    fun layout(screenObject: ScreenObject)

    fun draw(screenObject: ScreenObject)

    fun bind(screenObject: ScreenObject)

    fun unbind(screenObject: ScreenObject)
}
