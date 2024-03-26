package com.haishinkit.amf

import android.util.Log
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.util.Date
import java.util.IllegalFormatFlagsException

internal class Amf0TypeBuffer(private val buffer: ByteBuffer) {
    val data: Any?
        get() {
            val marker = buffer.get()

            when (marker) {
                NUMBER -> {
                    buffer.position(buffer.position() - 1)
                    return number
                }

                BOOL -> {
                    buffer.position(buffer.position() - 1)
                    return boolean
                }

                STRING -> {
                    buffer.position(buffer.position() - 1)
                    return string
                }

                OBJECT -> {
                    buffer.position(buffer.position() - 1)
                    return map
                }

                MOVIECLIP -> throw UnsupportedOperationException()
                NULL -> return null
                UNDEFINED -> return Amf0Undefined
                REFERENCE -> throw UnsupportedOperationException()
                ECMA_ARRAY -> {
                    buffer.position(buffer.position() - 1)
                    return array
                }

                OBJECT_END -> throw UnsupportedOperationException()
                STRICT_ARRAY -> {
                    buffer.position(buffer.position() - 1)
                    return list
                }

                DATE -> {
                    buffer.position(buffer.position() - 1)
                    return date
                }

                LONG_STRING -> {
                    buffer.position(buffer.position() - 1)
                    return string
                }

                UNSUPPORTED -> throw UnsupportedOperationException()
                RECORDSET -> throw UnsupportedOperationException()
                XML_DOCUMENT -> {
                    buffer.position(buffer.position() - 1)
                    return xmlDocument
                }

                TYPED_OBJECT -> throw UnsupportedOperationException()
                AVMPLUSH -> throw UnsupportedOperationException()
                else -> {
                }
            }

            return null
        }

    val boolean: Boolean
        get() {
            val marker = buffer.get()
            if (marker != BOOL) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            return buffer.get().toInt() == 1
        }

    val number: Double
        get() {
            val marker = buffer.get()
            if (marker != NUMBER) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            return buffer.double
        }

    val string: String
        get() {
            when (val marker = buffer.get()) {
                STRING, LONG_STRING -> {
                    return getString(STRING == marker)
                }

                else -> throw IllegalFormatFlagsException(marker.toString())
            }
        }

    val map: Map<String, Any?>?
        get() {
            val marker = buffer.get()
            if (marker == NULL) {
                return null
            }
            if (marker != OBJECT) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            val map = HashMap<String, Any?>()
            while (true) {
                val key = getString(true)
                if (key == "") {
                    buffer.get()
                    break
                }
                map[key] = data
            }
            return map
        }

    val list: List<Any?>?
        get() {
            val marker = buffer.get()
            if (marker == NULL) {
                return null
            }
            if (marker != STRICT_ARRAY) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            val count = buffer.int
            val result = mutableListOf<Any?>()
            for (i in 0 until count) {
                result.add(data)
            }
            return result
        }

    val array: Amf0EcmaArray?
        get() {
            val marker = buffer.get()
            if (marker == NULL) {
                return null
            }
            if (marker != ECMA_ARRAY) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            buffer.int
            val array = Amf0EcmaArray()
            while (true) {
                val key = getString(true)
                if (key == "") {
                    buffer.get()
                    break
                }
                array[key] = data
            }
            return array
        }

    // timezone
    val date: Date
        get() {
            val marker = buffer.get()
            if (marker != DATE) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            val value = buffer.double
            buffer.position(buffer.position() + 2)
            val date = Date()
            date.time = value.toLong()
            return date
        }

    val xmlDocument: Amf0XmlDocument
        get() {
            val marker = buffer.get()
            if (marker != XML_DOCUMENT) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            return Amf0XmlDocument(getString(false))
        }

    fun putBoolean(value: Boolean): Amf0TypeBuffer {
        buffer.put(BOOL)
        buffer.put((if (value) 1 else 0).toByte())
        return this
    }

    fun putNumber(value: Double): Amf0TypeBuffer {
        buffer.put(NUMBER)
        buffer.putDouble(value)
        return this
    }

    fun putString(value: String?): Amf0TypeBuffer {
        if (value == null) {
            buffer.put(NULL)
            return this
        }
        val length = value.length
        val isShort = length <= java.lang.Short.MAX_VALUE.toInt()
        buffer.put(if (isShort) STRING else LONG_STRING)
        return putString(value, isShort)
    }

    fun putMap(value: Map<String, Any?>?): Amf0TypeBuffer {
        if (value == null) {
            buffer.put(NULL)
            return this
        }
        buffer.put(OBJECT)
        for ((key, value1) in value) {
            putString(key, true).putData(value1)
        }
        putString("", true)
        buffer.put(OBJECT_END)
        return this
    }

    fun putDate(value: Date?): Amf0TypeBuffer {
        if (value == null) {
            buffer.put(NULL)
            return this
        }
        buffer.put(DATE)
        buffer.putDouble(value.time.toDouble())
        buffer.put(byteArrayOf(0x00, 0x00))
        return this
    }

    fun putList(value: List<Any>?): Amf0TypeBuffer {
        if (value == null) {
            buffer.put(NULL)
            return this
        }
        buffer.put(STRICT_ARRAY)
        buffer.putInt(value.size)
        for (data in value) {
            putData(data)
        }
        return this
    }

    fun putEcmaArray(value: Amf0EcmaArray?): Amf0TypeBuffer {
        if (value == null) {
            buffer.put(NULL)
            return this
        }
        buffer.put(ECMA_ARRAY)
        buffer.putInt(value.size)
        for (i in value) {
            putString(i, true)
            putData(value[i])
        }
        putString("", true)
        buffer.put(OBJECT_END)
        return this
    }

    fun putData(value: Any?): Amf0TypeBuffer {
        if (value == null) {
            buffer.put(NULL)
            return this
        }
        if (value is String) {
            return putString(value as String?)
        }
        if (value is Double) {
            return putNumber((value as Double?)!!)
        }
        if (value is Int) {
            return putNumber(value.toDouble())
        }
        if (value is Short) {
            return putNumber(value.toDouble())
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
        if (value is Amf0EcmaArray) {
            return putEcmaArray(value)
        }
        if (value is Amf0Undefined) {
            buffer.put(UNDEFINED)
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
    ): Amf0TypeBuffer {
        val length = value.length
        if (asShort) {
            buffer.putShort(length.toShort())
        } else {
            buffer.putInt(length)
        }
        buffer.put(value.toByteArray())
        return this
    }

    private fun getString(asShort: Boolean): String {
        val length = if (asShort) buffer.short.toInt() else buffer.int
        return try {
            val bytes = ByteArray(length)
            buffer.get(bytes)
            String(bytes, charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            Log.e(javaClass.name, e.toString())
            ""
        }
    }

    companion object {
        const val NUMBER: Byte = 0x00
        const val BOOL: Byte = 0x01
        const val STRING: Byte = 0x02
        const val OBJECT: Byte = 0x03
        const val MOVIECLIP: Byte = 0x04
        const val NULL: Byte = 0x05
        const val UNDEFINED: Byte = 0x06
        const val REFERENCE: Byte = 0x07
        const val ECMA_ARRAY: Byte = 0x08
        const val OBJECT_END: Byte = 0x09
        const val STRICT_ARRAY: Byte = 0x0a
        const val DATE: Byte = 0x0b
        const val LONG_STRING: Byte = 0x0c
        const val UNSUPPORTED: Byte = 0x0d
        const val RECORDSET: Byte = 0x0e
        const val XML_DOCUMENT: Byte = 0x0f
        const val TYPED_OBJECT: Byte = 0x10
        const val AVMPLUSH: Byte = 0x11
    }
}
