package com.haishinkit.util

import java.util.concurrent.ConcurrentHashMap

/**
 * The util object to get feature flag info.
 */
object FeatureUtil {
    const val FEATURE_BITRATE_CHANGE = 0
    const val FEATURE_VULKAN_PIXEL_TRANSFORM = 1

    private var flags = ConcurrentHashMap(
        mutableMapOf(
            FEATURE_BITRATE_CHANGE to true,
            FEATURE_VULKAN_PIXEL_TRANSFORM to false
        )
    )

    /**
     * Whether or not a flag is enabled.
     */
    fun isEnabled(feature: Int): Boolean {
        return flags[feature] ?: false
    }

    /**
     * Setter for feature flag.
     */
    fun setEnabled(feature: Int, isEnabled: Boolean) {
        flags[feature] = isEnabled
    }
}
