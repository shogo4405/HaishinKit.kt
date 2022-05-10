package com.haishinkit.graphics

internal class ChoreographerFpsController : FpsController {
    override var frameRate: Int = DEFAULT_FRAME_RATE
        set(value) {
            field = 60.coerceAtMost(1.coerceAtLeast(value))
            elapsed = 1000000000 / field.toLong()
        }

    private var timestamp: Long = 0
    private var elapsed: Long = 1000000000 / 60

    override fun advanced(frameTime: Long): Boolean {
        if (timestamp == 0L) {
            timestamp = frameTime
        }
        if (elapsed <= (frameTime - timestamp)) {
            timestamp = frameTime
            return true
        }
        return true
    }

    override fun timestamp(frameTime: Long): Long {
        return frameTime
    }

    override fun clear() {
        timestamp = 0
    }

    companion object {
        const val DEFAULT_FRAME_RATE = 60
    }
}
