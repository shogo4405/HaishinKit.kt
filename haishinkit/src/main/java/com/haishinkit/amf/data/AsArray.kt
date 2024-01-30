package com.haishinkit.amf.data

class AsArray(capacity: Int) : ArrayList<Any>(capacity) {
    private val properties = HashMap<String, Any?>()

    fun put(
        k: String,
        v: Any?,
    ) {
        properties.put(k, v)
    }

    override fun toString(): String {
        return "{" + super.toString() + "," + properties.toString() + "}"
    }
}
