package com.haishinkit.util

import java.util.HashMap
import java.util.Objects

import com.haishinkit.events.Event

object EventUtils {
    fun toMap(event: Event): Map<String, Any> {
        val data = event.data
        if (data == null || data !is Map<*, *>) {
            return HashMap()
        }
        return data as Map<String, Any>
    }
}
