package com.haishinkit.graphics

import com.haishinkit.gles.GlThreadPixelTransform
import com.haishinkit.vk.VkPixelTransform

internal class PixelTransformFactory {
    fun create(useGLES: Boolean = false): PixelTransform {
        if (!useGLES && VkPixelTransform.isSupported()) {
            return VkPixelTransform()
        }
        return GlThreadPixelTransform()
    }
}
