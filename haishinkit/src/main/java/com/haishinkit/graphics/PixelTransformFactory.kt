package com.haishinkit.graphics

import com.haishinkit.util.FeatureUtil

internal class PixelTransformFactory {
    fun create(): PixelTransform {
        if (FeatureUtil.isEnabled(FeatureUtil.FEATURE_VULKAN_PIXEL_TRANSFORM) && VkPixelTransform.isSupported()) {
            return VkPixelTransform()
        }
        return GlThreadPixelTransform()
    }
}
