package com.haishinkit.amf.data

import java.util.ArrayList
import java.util.HashMap

final class ASArray(capacity: Int) : ArrayList<Any>(capacity) {
    private val properties = HashMap<String, Any?>()

    fun put(k: String, v: Any?) {
        properties.put(k, v)
    }

    override fun toString(): String {
        return "{" + super.toString() + "," + properties.toString() + "}"
    }
}
