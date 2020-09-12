package com.haishinkit.media

import org.apache.commons.lang3.builder.ToStringBuilder

data class AudioSetting(var bitrate: Int) {
    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
