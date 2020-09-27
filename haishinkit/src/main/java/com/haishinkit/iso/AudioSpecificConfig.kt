package com.haishinkit.iso

import org.apache.commons.lang3.builder.ToStringBuilder
import java.nio.ByteBuffer

/**
 * The Audio Specific Config is the global header for MPEG-4 Audio
 * @see http://wiki.multimedia.cx/index.php?title=MPEG-4_Audio.Audio_Specific_Config
 * @see http://wiki.multimedia.cx/?title=Understanding_AAC
 */
data class AudioSpecificConfig(
    val type: AudioObjectType,
    val frequency: SamplingFrequency,
    val channel: ChannelConfiguration
) {
    enum class AudioObjectType(val rawValue: Byte) {
        UNKNOWN(0x00),
        AAC_MAIN(0x01),
        AAC_LC(0x02),
        AAC_SSL(0x03),
        AAC_LTP(0x04),
        AAC_SBR(0x05),
        AAC_SCALABLE(0x06),
        TWINQVQ(0x07),
        CELP(0x08),
        HXVC(0x09);
    }

    enum class SamplingFrequency(val rawValue: Byte) {
        HZ96000(0x00),
        HZ88200(0x01),
        HZ64000(0x02),
        HZ48000(0x03),
        HZ44100(0x04),
        HZ32000(0x05),
        HZ24000(0x06),
        HZ22050(0x07),
        HZ16000(0x08),
        HZ12000(0x09),
        HZ11025(0x0A),
        HZ8000(0x0B),
        HZ7350(0x0C);
    }

    enum class ChannelConfiguration(val rawValue: Byte) {
        DEFINE_IN_AOT_SPECIFIC_CONFIG(0x00),
        FRONT_OF_CENTER(0x01),
        FRONT_LEFT_AND_FRONT_RIGHT(0x02),
        FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT(0x03),
        FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT_AND_BACK_CENTER(0x04),
        FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT_AND_BACK_LEFT_AND_BACK_RIGHT(0x05),
        FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT_AND_BACK_LEFT_AND_BACK_RIGHT_LFE(0x06),
        FRONT_CENTER_AND_FRONT_LEFT_AND_FRONT_RIGHT_AND_SIDE_LEFT_AND_RIGHT_AND_BACK_RIGHT_LFE(0x07),
        UNKNOWN(Byte.MAX_VALUE);
    }

    fun toADTS(length: Int): ByteArray {
        val fullSize = ADTS_HEADER_SIZE + length
        val adts = ByteArray(ADTS_HEADER_SIZE)
        adts[0] = 0xFF.toByte()
        adts[1] = 0xF9.toByte()
        adts[2] = (type.rawValue - 1 shl 6 or (frequency.rawValue.toInt() shl 2).toInt() or (channel.rawValue.toInt() shr 2).toInt()).toByte()
        adts[3] = (channel.rawValue.toInt() and 3 shl 6 or (fullSize shr 11)).toByte()
        adts[4] = (fullSize and 0x7FF shr 3).toByte()
        adts[5] = (fullSize and 7 shl 5 or 0x1F).toByte()
        adts[6] = 0xFC.toByte()
        return adts
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val ADTS_HEADER_SIZE = 7

        internal fun create(buffer: ByteBuffer): AudioSpecificConfig {
            return AudioSpecificConfig(
                type = AudioObjectType.values().first { n -> n.rawValue.toInt() == buffer[0].toInt() shr 3 },
                frequency = SamplingFrequency.values().first { n -> n.rawValue.toInt() == (buffer[0].toInt() and 7 shl 1 or (buffer[1].toInt() and 0xFF shr 7)) },
                channel = ChannelConfiguration.values().first { n -> n.rawValue.toInt() == buffer[1].toInt() and 120 shr 3 }
            )
        }
    }
}
