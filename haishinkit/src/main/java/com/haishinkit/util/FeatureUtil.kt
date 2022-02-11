package com.haishinkit.util

import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * The util object to get feature flag info.
 */
object FeatureUtil {
    const val FEATURE_BITRATE_CHANGE = "bitrate_change"
    const val FEATURE_VULKAN_PIXEL_TRANSFORM = "vulkan_pixel_transform"

    private var flags = ConcurrentHashMap(
        mutableMapOf(
            FEATURE_BITRATE_CHANGE to true,
            FEATURE_VULKAN_PIXEL_TRANSFORM to false
        )
    )

    /**
     * Whether or not a flag is enabled.
     */
    fun isEnabled(feature: String): Boolean {
        return flags[feature] ?: false
    }

    /**
     * Setter for feature flag.
     */
    fun setEnabled(feature: String, isEnabled: Boolean) {
        flags[feature] = isEnabled
    }

    override fun toString(): String {
        return JSONObject(flags.toMap()).toString()
    }
}
