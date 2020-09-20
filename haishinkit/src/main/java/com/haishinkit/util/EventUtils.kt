package com.haishinkit.util

import com.haishinkit.events.Event
import java.util.HashMap

object EventUtils {
    fun toMap(event: Event): Map<String, Any> {
        val data = event.data
        if (data == null || data !is Map<*, *>) {
            return HashMap()
        }
        return data as Map<String, Any>
    }
}
