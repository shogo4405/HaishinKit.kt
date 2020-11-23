package com.haishinkit.codec.util

interface FpsController {
    fun advanced(timestamp: Long): Boolean
    fun timestamp(timestamp: Long): Long
    fun clear()
}
