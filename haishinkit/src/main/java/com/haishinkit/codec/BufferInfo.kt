package com.haishinkit.codec

import org.apache.commons.lang3.builder.ToStringBuilder

data class BufferInfo(
    val type: BufferType,
    val presentationTimeUs: Long,
    val width: Int = -1,
    val height: Int = -1,
    val rowStride: Int = -1,
    val pixelStride: Int = -1,
    val rotation: Int = 0
) {
    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
