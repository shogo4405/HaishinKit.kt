package com.haishinkit.metric

import android.util.Log
import kotlin.math.pow
import kotlin.math.sqrt

class FrameTracker {
    private var audioTimestamp = DEFAULT_TIMESTAMP
    private var audioTimestamps = ArrayList<Long>()
    private var audioRotatedTimestamp = DEFAULT_TIMESTAMP
    private var videoTimestamp = DEFAULT_TIMESTAMP
    private var videoTimestamps = ArrayList<Long>()
    private var videoRotatedTimestamp = DEFAULT_TIMESTAMP

    fun track(type: Int, timestamp: Long) {
        when (type) {
            TYPE_AUDIO -> {
                print(TYPE_AUDIO, timestamp)
            }
            TYPE_VIDEO -> {
                print(TYPE_VIDEO, timestamp)
            }
        }
    }

    fun clear() {
        audioTimestamp = DEFAULT_TIMESTAMP
        audioRotatedTimestamp = DEFAULT_TIMESTAMP
        audioTimestamps.clear()
        videoTimestamp = DEFAULT_TIMESTAMP
        videoRotatedTimestamp = DEFAULT_TIMESTAMP
        videoTimestamps.clear()
    }

    private fun print(type: Int, timestamp: Long) {
        var diff = 0L
        when (type) {
            TYPE_AUDIO -> {
                if (audioTimestamp == -1L) {
                    diff = 0
                    audioRotatedTimestamp = timestamp
                } else {
                    diff = timestamp - audioTimestamp
                }
                if (1000 < timestamp - audioRotatedTimestamp) {
                    if (VERBOSE) {
                        Log.d(TAG, "audio stats: average=${average(audioTimestamps)}, sd=${sd(audioTimestamps)}")
                    }
                    audioTimestamps.clear()
                    audioRotatedTimestamp = timestamp
                }
                audioTimestamps.add(diff)
                audioTimestamp = timestamp
            }
            TYPE_VIDEO -> {
                if (videoTimestamp == -1L) {
                    diff = 0
                    videoTimestamp = timestamp
                } else {
                    diff = timestamp - videoTimestamp
                }
                if (1000 < timestamp - videoRotatedTimestamp) {
                    if (VERBOSE) {
                        Log.d(TAG, "video stats: average=${average(videoTimestamps)}, sd=${sd(videoTimestamps)}")
                    }
                    videoTimestamps.clear()
                    videoRotatedTimestamp = timestamp
                }
                videoTimestamps.add(diff)
                videoTimestamp = timestamp
            }
        }
    }

    private fun average(timestamps: List<Long>): Double {
        return timestamps.average()
    }

    private fun sd(timestamps: List<Long>): Double {
        val mean = average(timestamps)
        return sqrt(timestamps.fold(0.0, { accumulator, next -> accumulator + (next - mean).pow(2.0) }) / timestamps.size )
    }

    companion object {
        const val TYPE_AUDIO = 0
        const val TYPE_VIDEO = 1

        const val VERBOSE = true
        private val TAG = FrameTracker::class.java.simpleName
        private const val DEFAULT_TIMESTAMP = -1L
    }
}
