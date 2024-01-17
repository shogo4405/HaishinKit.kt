package com.haishinkit.screen

interface ScreenRenderer {
    /**
     * Specifies the deviceOrientation that describe the physical orientation of the device.
     */
    var deviceOrientation: Int

    val shouldInvalidateLayout: Boolean
    fun layout(screenObject: ScreenObject)

    fun draw(screenObject: ScreenObject)

    fun bind(screenObject: ScreenObject)

    fun unbind(screenObject: ScreenObject)

    fun invalidateLayout()
}
