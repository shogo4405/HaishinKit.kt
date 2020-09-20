package com.haishinkit.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import java.io.IOException

internal class AACEncoder : EncoderBase(MIME) {
    var sampleRate: Int = DEFAULT_SAMPLE_RATE
    var channelCount: Int = DEFAULT_CHANNEL_COUNT
    var bitRate: Int = DEFAULT_BIT_RATE

    @Throws(IOException::class)
    override fun createMediaCodec(): MediaCodec {
        val codec = MediaCodec.createEncoderByType(MIME)
        val mediaFormat = MediaFormat.createAudioFormat(MIME, sampleRate, channelCount)
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        return codec
    }

    companion object {
        const val MIME = "audio/mp4a-latm"

        const val DEFAULT_SAMPLE_RATE: Int = 44100
        const val DEFAULT_CHANNEL_COUNT: Int = 1
        const val DEFAULT_BIT_RATE: Int = 64000
    }
}
