package com.haishinkit.graphics

import android.os.Build
import com.haishinkit.util.FeatureUtil

internal class PixelTransformFactory {
    fun create(): PixelTransform {
        if (FeatureUtil.isEnabled(FeatureUtil.FEATURE_VULKAN_PIXEL_TRANSFORM)) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && VkPixelTransform.isSupported()) {
                VkPixelTransform()
            } else {
                GlThreadPixelTransform()
            }
        }
        return GlThreadPixelTransform()
    }
}
