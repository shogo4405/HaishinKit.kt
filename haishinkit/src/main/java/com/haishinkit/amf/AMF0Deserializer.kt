package com.haishinkit.amf

import android.util.Log
import com.haishinkit.amf.data.ASArray
import com.haishinkit.amf.data.ASUndefined
import com.haishinkit.amf.data.ASXMLDocument
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.util.Date
import java.util.HashMap
import java.util.IllegalFormatFlagsException

internal class AMF0Deserializer(private val buffer: ByteBuffer) {
    val `object`: Any?
        get() {
            val marker = buffer.get()

            when (marker) {
                AMF0Marker.NUMBER.rawValue -> {
                    buffer.position(buffer.position() - 1)
                    return double
                }
                AMF0Marker.BOOL.rawValue -> {
                    buffer.position(buffer.position() - 1)
                    return boolean
                }
                AMF0Marker.STRING.rawValue -> {
                    buffer.position(buffer.position() - 1)
                    return string
                }
                AMF0Marker.OBJECT.rawValue -> {
                    buffer.position(buffer.position() - 1)
                    return map
                }
                AMF0Marker.MOVIECLIP.rawValue -> throw UnsupportedOperationException()
                AMF0Marker.NULL.rawValue -> return null
                AMF0Marker.UNDEFINED.rawValue -> return ASUndefined.instance
                AMF0Marker.REFERENCE.rawValue -> throw UnsupportedOperationException()
                AMF0Marker.ECMAARRAY.rawValue -> {
                    buffer.position(buffer.position() - 1)
                    return list
                }
                AMF0Marker.OBJECTEND.rawValue -> throw UnsupportedOperationException()
                AMF0Marker.STRICTARRAY.rawValue -> {
                    buffer.position(buffer.position() - 1)
                    return objects
                }
                AMF0Marker.DATE.rawValue -> {
                    buffer.position(buffer.position() - 1)
                    return date
                }
                AMF0Marker.LONGSTRING.rawValue -> {
                    buffer.position(buffer.position() - 1)
                    return string
                }
                AMF0Marker.UNSUPPORTED.rawValue -> throw UnsupportedOperationException()
                AMF0Marker.RECORDSET.rawValue -> throw UnsupportedOperationException()
                AMF0Marker.XMLDOCUMENT.rawValue -> {
                    buffer.position(buffer.position() - 1)
                    return xmlDocument
                }
                AMF0Marker.TYPEDOBJECT.rawValue -> throw UnsupportedOperationException()
                AMF0Marker.AVMPLUSH.rawValue -> throw UnsupportedOperationException()
                else -> {
                }
            }

            return null
        }

    val boolean: Boolean
        get() {
            val marker = buffer.get()
            if (marker != AMF0Marker.BOOL.rawValue) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            return buffer.get().toInt() == 1
        }

    val double: Double
        get() {
            val marker = buffer.get()
            if (marker != AMF0Marker.NUMBER.rawValue) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            return buffer.double
        }

    val string: String
        get() {
            val marker = buffer.get()
            when (marker) {
                AMF0Marker.STRING.rawValue, AMF0Marker.LONGSTRING.rawValue -> {
                }
                else -> throw IllegalFormatFlagsException(marker.toString())
            }
            return getString(AMF0Marker.STRING.rawValue == marker)
        }

    val map: Map<String, Any?>?
        get() {
            val marker = buffer.get()
            if (marker == AMF0Marker.NULL.rawValue) {
                return null
            }
            if (marker != AMF0Marker.OBJECT.rawValue) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            val map = HashMap<String, Any?>()
            while (true) {
                val key = getString(true)
                if (key == "") {
                    buffer.get()
                    break
                }
                map.put(key, `object`)
            }
            return map
        }

    val objects: Array<Any?>?
        get() {
            val marker = buffer.get()
            if (marker == AMF0Marker.NULL.rawValue) {
                return null
            }
            if (marker != AMF0Marker.STRICTARRAY.rawValue) {
                throw IllegalFormatFlagsException(marker.toString())
            }

            val count = buffer.int
            val objects = arrayOfNulls<Any>(count)
            for (i in 0..count - 1) {
                objects[i] = `object`
            }

            return objects
        }

    val list: List<Any?>?
        get() {
            val marker = buffer.get()
            if (marker == AMF0Marker.NULL.rawValue) {
                return null
            }
            if (marker != AMF0Marker.ECMAARRAY.rawValue) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            val count = buffer.int
            val array = ASArray(count)
            while (true) {
                val key = getString(true)
                if (key == "") {
                    buffer.get()
                    break
                }
                array.put(key, `object`)
            }
            return array
        }

    // timezone
    val date: Date
        get() {
            val marker = buffer.get()
            if (marker != AMF0Marker.DATE.rawValue) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            val value = buffer.double
            buffer.position(buffer.position() + 2)
            val date = Date()
            date.time = value.toLong()
            return date
        }

    val xmlDocument: ASXMLDocument
        get() {
            val marker = buffer.get()
            if (marker != AMF0Marker.XMLDOCUMENT.rawValue) {
                throw IllegalFormatFlagsException(marker.toString())
            }
            return ASXMLDocument(getString(false))
        }

    private fun getString(asShort: Boolean): String {
        var length = if (asShort) buffer.short.toInt() else buffer.int
        return try {
            val bytes = ByteArray(length)
            buffer.get(bytes)
            String(bytes, charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            Log.e(javaClass.getName(), e.toString())
            ""
        }
    }
}
