package com.haishinkit.codec

interface ByteConverter {
    fun convert(input: ByteArray): ByteArray
}
