package com.haishinkit.yuv

import com.haishinkit.codec.BufferInfo
import com.haishinkit.codec.ByteConverter

class NullByteConverter : ByteConverter {
    override fun convert(input: ByteArray, info: BufferInfo): ByteArray {
        return input
    }

    companion object {
        val instance = NullByteConverter()
    }
}
