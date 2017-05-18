package com.haishinkit.media.codec

interface ByteConverter {
    fun convert(input: ByteArray): ByteArray
}
