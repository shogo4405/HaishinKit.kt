package com.haishinkit.iso

import android.media.MediaFormat
import com.haishinkit.codec.CodecOption
import com.haishinkit.codec.VideoCodec
import java.nio.ByteBuffer

internal interface DecoderConfigurationRecord {
    val capacity: Int
    fun encode(buffer: ByteBuffer): DecoderConfigurationRecord
    fun configure(codec: VideoCodec): Boolean {
        codec.options = toCodecSpecificData(codec.options)
        return true
    }

    fun toByteBuffer(): ByteBuffer {
        return ByteBuffer.allocate(capacity).apply {
            encode(this)
            flip()
        }
    }

    fun toCodecSpecificData(options: List<CodecOption>): List<CodecOption>

    companion object {
        fun create(mime: String, mediaFormat: MediaFormat): DecoderConfigurationRecord? = when (mime) {
            MediaFormat.MIMETYPE_VIDEO_AVC -> AvcDecoderConfigurationRecord.create(mediaFormat)
            MediaFormat.MIMETYPE_VIDEO_HEVC -> HevcDecoderConfigurationRecord.create(mediaFormat)
            else -> null
        }

        fun decode(mime: String, buffer: ByteBuffer): DecoderConfigurationRecord? = when (mime) {
            MediaFormat.MIMETYPE_VIDEO_AVC -> AvcDecoderConfigurationRecord.decode(buffer)
            MediaFormat.MIMETYPE_VIDEO_HEVC -> HevcDecoderConfigurationRecord.decode(buffer)
            else -> null
        }
    }
}
