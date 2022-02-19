package com.haishinkit.codec

import android.media.MediaFormat
import java.nio.ByteBuffer

data class CodecOption(var key: String, var value: Any) {
    internal fun apply(format: MediaFormat) {
        when (true) {
            value is Int -> format.setInteger(key, value as Int)
            value is Long -> format.setLong(key, value as Long)
            value is Float -> format.setFloat(key, value as Float)
            value is String -> format.setString(key, value as String)
            value is ByteBuffer -> format.setByteBuffer(key, value as ByteBuffer)
            else -> throw IllegalArgumentException()
        }
    }
}
