package com.haishinkit.graphics.util

internal interface FpsController {
    /**
     * Specifies the frameRate for an output source in frames/sec.
     */
    var frameRate: Int

    fun advanced(timestamp: Long): Boolean
    fun timestamp(timestamp: Long): Long
    fun clear()
}
