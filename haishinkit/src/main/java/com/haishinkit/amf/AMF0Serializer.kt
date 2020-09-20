package com.haishinkit.amf

import com.haishinkit.amf.data.ASUndefined
import java.nio.ByteBuffer
import java.util.Date

internal class AMF0Serializer(private val buffer: ByteBuffer) {

    fun putBoolean(value: Boolean): AMF0Serializer {
        buffer.put(AMF0Marker.BOOL.rawValue)
        buffer.put((if (value) 1 else 0).toByte())
        return this
    }

    fun putDouble(value: Double): AMF0Serializer {
        buffer.put(AMF0Marker.NUMBER.rawValue)
        buffer.putDouble(value)
        return this
    }

    fun putString(value: String?): AMF0Serializer {
        if (value == null) {
            buffer.put(AMF0Marker.NULL.rawValue)
            return this
        }
        val length = value.length
        val isShort = if (length <= java.lang.Short.MAX_VALUE.toInt()) true else false
        buffer.put(if (isShort) AMF0Marker.STRING.rawValue else AMF0Marker.LONGSTRING.rawValue)
        return putString(value, isShort)
    }

    fun putMap(value: Map<String, Any?>?): AMF0Serializer {
        if (value == null) {
            buffer.put(AMF0Marker.NULL.rawValue)
            return this
        }
        buffer.put(AMF0Marker.OBJECT.rawValue)
        for ((key, value1) in value) {
            putString(key, true).putObject(value1)
        }
        putString("", true)
        buffer.put(AMF0Marker.OBJECTEND.rawValue)
        return this
    }

    fun putDate(value: Date?): AMF0Serializer {
        if (value == null) {
            buffer.put(AMF0Marker.NULL.rawValue)
            return this
        }
        buffer.put(AMF0Marker.DATE.rawValue)
        buffer.putDouble(value.time.toDouble())
        buffer.put(byteArrayOf(0x00, 0x00))
        return this
    }

    fun putList(value: List<Any>?): AMF0Serializer {
        if (value == null) {
            buffer.put(AMF0Marker.NULL.rawValue)
            return this
        }
        buffer.put(AMF0Marker.ECMAARRAY.rawValue)
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

    fun putObject(value: Any?): AMF0Serializer {
        if (value == null) {
            buffer.put(AMF0Marker.NULL.rawValue)
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
        if (value is ASUndefined) {
            buffer.put(AMF0Marker.UNDEFINED.rawValue)
            return this
        }
        return this
    }

    override fun toString(): String {
        return buffer.toString()
    }

    private fun putString(value: String, asShort: Boolean): AMF0Serializer {
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
