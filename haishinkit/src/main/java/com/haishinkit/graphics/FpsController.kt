package com.haishinkit.graphics

internal interface FpsController {
    /**
     * Specifies the frameRate for an output source in frames/sec.
     */
    var frameRate: Int

    fun advanced(frameTime: Long): Boolean

    fun timestamp(frameTime: Long): Long

    fun clear()
}
