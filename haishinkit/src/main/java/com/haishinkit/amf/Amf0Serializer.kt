package com.haishinkit.amf

import com.haishinkit.amf.data.AsUndefined
import java.nio.ByteBuffer
import java.util.Date

internal class Amf0Serializer(private val buffer: ByteBuffer) {
    fun putBoolean(value: Boolean): Amf0Serializer {
        buffer.put(Amf0Marker.BOOL.rawValue)
        buffer.put((if (value) 1 else 0).toByte())
        return this
    }

    fun putDouble(value: Double): Amf0Serializer {
        buffer.put(Amf0Marker.NUMBER.rawValue)
        buffer.putDouble(value)
        return this
    }

    fun putString(value: String?): Amf0Serializer {
        if (value == null) {
            buffer.put(Amf0Marker.NULL.rawValue)
            return this
        }
        val length = value.length
        val isShort = length <= java.lang.Short.MAX_VALUE.toInt()
        buffer.put(if (isShort) Amf0Marker.STRING.rawValue else Amf0Marker.LONGSTRING.rawValue)
        return putString(value, isShort)
    }

    fun putMap(value: Map<String, Any?>?): Amf0Serializer {
        if (value == null) {
            buffer.put(Amf0Marker.NULL.rawValue)
            return this
        }
        buffer.put(Amf0Marker.OBJECT.rawValue)
        for ((key, value1) in value) {
            putString(key, true).putObject(value1)
        }
        putString("", true)
        buffer.put(Amf0Marker.OBJECTEND.rawValue)
        return this
    }

    fun putDate(value: Date?): Amf0Serializer {
        if (value == null) {
            buffer.put(Amf0Marker.NULL.rawValue)
            return this
        }
        buffer.put(Amf0Marker.DATE.rawValue)
        buffer.putDouble(value.time.toDouble())
        buffer.put(byteArrayOf(0x00, 0x00))
        return this
    }

    fun putList(value: List<Any>?): Amf0Serializer {
        if (value == null) {
            buffer.put(Amf0Marker.NULL.rawValue)
            return this
        }
        buffer.put(Amf0Marker.ECMAARRAY.rawValue)
        if (value.isEmpty()) {
            buffer.put(byteArrayOf(0x00, 0x00, 0x00, 0x00))
            return this
        }
        buffer.putInt(value.size)
        for (`object` in value) {
            putObject(`object`)
        }
        return this
    }

    fun putObject(value: Any?): Amf0Serializer {
        if (value == null) {
            buffer.put(Amf0Marker.NULL.rawValue)
            return this
        }
        if (value is String) {
            return putString(value as String?)
        }
        if (value is Double) {
            return putDouble((value as Double?)!!)
        }
        if (value is Int) {
            return putDouble(value.toDouble())
        }
        if (value is Short) {
            return putDouble(value.toDouble())
        }
        if (value is Boolean) {
            return putBoolean((value as Boolean?)!!)
        }
        if (value is Date) {
            return putDate(value as Date?)
        }
        if (value is Map<*, *>) {
            return putMap(value as Map<String, Any>?)
        }
        if (value is List<*>) {
            return putList(value as List<Any>?)
        }
        if (value is AsUndefined) {
            buffer.put(Amf0Marker.UNDEFINED.rawValue)
            return this
        }
        return this
    }

    override fun toString(): String {
        return buffer.toString()
    }

    private fun putString(
        value: String,
        asShort: Boolean,
    ): Amf0Serializer {
        val length = value.length
        if (asShort) {
            buffer.putShort(length.toShort())
        } else {
            buffer.putInt(length)
        }
        buffer.put(value.toByteArray())
        return this
    }
}
