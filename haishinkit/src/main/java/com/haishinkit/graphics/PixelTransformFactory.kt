package com.haishinkit.graphics

internal class PixelTransformFactory {
    fun create(useGLES: Boolean = false): PixelTransform {
        if (!useGLES && VkPixelTransform.isSupported()) {
            return VkPixelTransform()
        }
        return GlThreadPixelTransform()
    }
}
