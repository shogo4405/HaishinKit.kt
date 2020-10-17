package com.haishinkit.codec

import android.media.MediaCodecInfo
import android.media.MediaFormat

internal class AudioCodec : MediaCodec(MIME) {
    var sampleRate = DEFAULT_SAMPLE_RATE
    var channelCount = DEFAULT_CHANNEL_COUNT
    var bitRate = DEFAULT_BIT_RATE

    override fun createOutputFormat(): MediaFormat {
        return MediaFormat.createAudioFormat(MIME, sampleRate, channelCount).apply {
            this.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            this.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        }
    }

    companion object {
        const val MIME = MediaCodec.MIME_AUDIO_MP4A

        const val DEFAULT_SAMPLE_RATE: Int = 44100
        const val DEFAULT_CHANNEL_COUNT: Int = 1
        const val DEFAULT_BIT_RATE: Int = 64000
    }
}
