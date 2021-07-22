package com.haishinkit.codec

import android.media.MediaCodecInfo
import android.media.MediaFormat
import org.apache.commons.lang3.builder.ToStringBuilder
import kotlin.properties.Delegates

class AudioCodec : MediaCodec(MIME) {
    @Suppress("unused")
    class Setting(var codec: AudioCodec? = null) : MediaCodec.Setting(codec) {
        var channelCount: Int by Delegates.observable(DEFAULT_CHANNEL_COUNT) { _, _, newValue ->
            codec?.channelCount = newValue
        }
        var bitRate: Int by Delegates.observable(DEFAULT_BIT_RATE) { _, _, newValue ->
            codec?.bitRate = newValue
        }
        var sampleRate: Int by Delegates.observable(DEFAULT_SAMPLE_RATE) { _, _, newValue ->
            codec?.sampleRate = newValue
        }
        override fun toString(): String {
            return ToStringBuilder.reflectionToString(this)
        }
    }

    var sampleRate = DEFAULT_SAMPLE_RATE
    var channelCount = DEFAULT_CHANNEL_COUNT
    var bitRate = DEFAULT_BIT_RATE
    var aacProfile = DEFAULT_AAC_PROFILE

    override fun createOutputFormat(): MediaFormat {
        return MediaFormat.createAudioFormat(MIME, sampleRate, channelCount).apply {
            if (mode == MODE_ENCODE) {
                setInteger(MediaFormat.KEY_AAC_PROFILE, aacProfile)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            } else {
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, DEFAULT_KEY_MAX_INPUT_SIZE)
            }
        }
    }

    companion object {
        const val MIME = MIME_AUDIO_MP4A

        const val DEFAULT_SAMPLE_RATE: Int = 44100
        const val DEFAULT_CHANNEL_COUNT: Int = 1
        const val DEFAULT_BIT_RATE: Int = 64000
        const val DEFAULT_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC

        const val DEFAULT_KEY_MAX_INPUT_SIZE = 1024 * 2
    }
}
