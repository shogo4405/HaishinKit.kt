package com.haishinkit.graphics.util

internal class NullFpsController : FpsController {
    override var frameRate: Int = DEFAULT_FRAME_RATE

    override fun advanced(timestamp: Long): Boolean {
        return true
    }

    override fun timestamp(timestamp: Long): Long {
        return timestamp
    }

    override fun clear() {
    }

    companion object {
        val instance = NullFpsController()

        const val DEFAULT_FRAME_RATE = 30
    }
}
