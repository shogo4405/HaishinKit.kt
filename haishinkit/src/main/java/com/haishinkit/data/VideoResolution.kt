package com.haishinkit.data

import org.apache.commons.lang3.builder.ToStringBuilder

/**
 * A data representing a video resolution.
 * @property width The video width.
 * @property height The video height.
 */
data class VideoResolution(val width: Int, val height: Int) {
    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
