package com.haishinkit.codec.util

/**
 * This source code from grafika.
 * https://github.com/google/grafika/blob/c747398a8f0d5c8ec7be2c66522a80b43dfc9a1e/app/src/main/java/com/android/grafika/ScheduledSwapActivity.java#L76
 */
class ScheduledFpsController : FpsController {
    private var framesAheadIndex = 2
    private var refreshPeriodNs = -1L
    private var holdFrames = 0
    private var updatePatternOffset = 0
    private var choreographerSkips = 0
    private var droppedFrames = 0
    private var previousRefreshNs: Long = 0
    private var updatePatternIdx = DEFAULT_UPDATE_PATTERN_INDEX
    private var position = 0
    private var speed = 0

    override fun advanced(timestamp: Long): Boolean {
        var draw = false

        if (1 < holdFrames) {
            holdFrames--
        } else {
            updatePatternOffset = (updatePatternOffset + 1) % UPDATE_PATTERNS[updatePatternIdx].length
            holdFrames = getHoldTime()
            draw = true
            position += speed
        }

        if (previousRefreshNs != 0L && timestamp - previousRefreshNs > refreshPeriodNs + ONE_MILLISECOND_NS) {
            choreographerSkips++
        }
        previousRefreshNs = timestamp

        val diff: Long = System.nanoTime() - timestamp
        if (diff > refreshPeriodNs - ONE_MILLISECOND_NS) {
            droppedFrames++
        }

        return draw
    }

    override fun timestamp(timestamp: Long): Long {
        val framesAhead = FRAME_AHEAD[framesAheadIndex]
        return timestamp + refreshPeriodNs * framesAhead
    }

    private fun getHoldTime(): Int {
        val ch: Char = UPDATE_PATTERNS[updatePatternIdx][updatePatternOffset]
        return ch - '0'
    }

    companion object {
        const val DEFAULT_UPDATE_PATTERN_INDEX = 3

        private val UPDATE_PATTERNS = arrayOf(
            "4", // 15 fps
            "32", // 24 fps
            "32322", // 25 fps
            "2", // 30 fps
            "2111", // 48 fps
            "1", // 60 fps
            "15" // erratic, useful for examination with systrace
        )
        private const val ONE_MILLISECOND_NS: Long = 1000000
        private val FRAME_AHEAD = intArrayOf( // sync with scheduledSwapAheadNames
            0, 1, 2, 3
        )
        private val TAG = ScheduledFpsController::class.java.simpleName
    }
}
