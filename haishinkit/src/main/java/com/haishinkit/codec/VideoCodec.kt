package com.haishinkit.codec

import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface

internal class VideoCodec() : Codec(MIME) {
    var bitRate = DEFAULT_BIT_RATE
    var frameRate = DEFAULT_FRAME_RATE
    var IFrameInterval = DEFAULT_I_FRAME_INTERVAL
    var width = DEFAULT_WIDTH
    var height = DEFAULT_HEIGHT
    var profile = DEFAULT_PROFILE
    var level = DEFAULT_LEVEL
    var colorFormat: Int = -1

    override fun createOutputFormat(): MediaFormat {
        return MediaFormat.createVideoFormat(MIME.rawValue, width, height).apply {
            this.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            this.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            this.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            this.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFrameInterval)
            this.setInteger(MediaFormat.KEY_PROFILE, profile)
            this.setInteger(MediaFormat.KEY_LEVEL, level)
        }
    }

    fun createInputSurface(): Surface? {
        return codec?.createInputSurface()
    }

    companion object {
        val MIME = com.haishinkit.codec.Codec.MIME.VIDEO_AVC

        const val DEFAULT_BIT_RATE = 500 * 1000
        const val DEFAULT_FRAME_RATE = 30
        const val DEFAULT_I_FRAME_INTERVAL = 2
        const val DEFAULT_WIDTH = 1024
        const val DEFAULT_HEIGHT = 768
        const val DEFAULT_PROFILE = MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline
        const val DEFAULT_LEVEL = MediaCodecInfo.CodecProfileLevel.AVCLevel31
    }
}
