package com.haishinkit.metric

class FrameTracker {
    private var audioTimestamp: Long = DEFAULT_TIMESTAMP
    private var audioTimestamps: ArrayList<Long> = ArrayList<Long>()
    private var videoTimestamp: Long = DEFAULT_TIMESTAMP
    private var videoTimestamps: ArrayList<Long> = ArrayList<Long>()

    fun track(type: Int, timestamp: Long) {
        when(type) {
            TYPE_AUDIO -> {
                audioTimestamps.add(timestamp)
                print(TYPE_AUDIO, timestamp)
            }
            TYPE_VIDEO -> {
                videoTimestamps.add(timestamp)
                print(TYPE_VIDEO, timestamp)
            }
        }
    }

    fun clear() {
        audioTimestamp = DEFAULT_TIMESTAMP
        audioTimestamps.clear()
        videoTimestamp = DEFAULT_TIMESTAMP
        videoTimestamps.clear()
    }

    private fun print(type: Int, timestamp: Long) {
    }

    companion object {
        const val TYPE_AUDIO = 0
        const val TYPE_VIDEO = 1

        private const val DEFAULT_TIMESTAMP = -1L
    }
}
