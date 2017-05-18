package com.haishinkit.util

import org.apache.commons.lang3.StringUtils

import java.nio.ByteBuffer

object ByteBufferUtils {

    fun toHexString(buffer: ByteBuffer?): String {
        if (buffer == null) {
            return StringUtils.EMPTY
        }
        val builder = StringBuilder()
        val slice = buffer.slice()
        for (i in 0..slice.limit() - 1) {
            builder.append(String.format("0x%02x,", slice.get().toInt() and 0xff))
        }
        return builder.toString()
    }
}
