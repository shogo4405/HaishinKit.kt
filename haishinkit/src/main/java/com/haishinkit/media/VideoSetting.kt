package com.haishinkit.media

import org.apache.commons.lang3.builder.ToStringBuilder

data class VideoSetting(
    var width: Int,
    var height: Int,
    var bitrate: Int
) {
    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
