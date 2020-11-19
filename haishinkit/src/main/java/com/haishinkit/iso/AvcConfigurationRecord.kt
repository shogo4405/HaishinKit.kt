package com.haishinkit.iso

import android.media.MediaFormat
import org.apache.commons.lang3.builder.ToStringBuilder
import java.nio.ByteBuffer
import java.util.ArrayList

data class AvcConfigurationRecord(
    val configurationVersion: Byte = 1,
    val avcProfileIndication: Byte = 0,
    val profileCompatibility: Byte = 0,
    val avcLevelIndication: Byte = 0,
    val lengthSizeMinusOneWithReserved: Byte = 0,
    val numOfSequenceParameterSetsWithReserved: Byte = 0,
    val sequenceParameterSets: List<ByteArray>? = null,
    val pictureParameterSets: List<ByteArray>? = null
) {
    val naluLength: Byte
        get() = ((lengthSizeMinusOneWithReserved.toInt() shr 6) + 1).toByte()

    fun toByteBuffer(): ByteBuffer {
        var capacity = 5

        sequenceParameterSets?.forEach { sps ->
            capacity += 3 + sps.size
        }
        pictureParameterSets?.forEach { pps ->
            capacity += 3 + pps.size
        }

        val buffer = ByteBuffer.allocate(capacity)
        buffer.put(configurationVersion)
        buffer.put(avcProfileIndication)
        buffer.put(profileCompatibility)
        buffer.put(avcLevelIndication)
        buffer.put(lengthSizeMinusOneWithReserved)

        // SPS
        buffer.put(numOfSequenceParameterSetsWithReserved)
        sequenceParameterSets?.forEach { sps ->
            buffer.putShort(sps.size.toShort())
            buffer.put(sps)
        }

        // PPS
        if (pictureParameterSets != null) {
            buffer.put(pictureParameterSets!!.size.toByte())
            pictureParameterSets?.forEach { pps ->
                buffer.putShort(pps.size.toShort())
                buffer.put(pps)
            }
            buffer.flip()
        }

        return buffer
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val RESERVE_LENGTH_SIZE_MINUS_ONE = 0x3F
        const val RESERVE_NUM_OF_SEQUENCE_PARAMETER_SETS = 0xE0
        const val RESERVE_CHROME_FORMAT = 0xFC
        const val RESERVE_BIT_DEPTH_LUMA_MINUS8 = 0xF8
        const val RESERVE_BIT_DEPTH_CHROME_MINUS8 = 0xF8

        internal fun create(mediaFormat: MediaFormat): AvcConfigurationRecord {
            // SPS => 0x00,0x00,0x00,0x01,0x67,0x42,0x00,0x29,0x8d,0x8d,0x40,0xa0,0xfd,0x00,0xf0,0x88,0x45,0x38
            val spsBuffer = mediaFormat.getByteBuffer("csd-0")
            // PPS => 0x00,0x00,0x00,0x01,0x68,0xca,0x43,0xc8
            val ppsBuffer = mediaFormat.getByteBuffer("csd-1")
            if (spsBuffer == null || ppsBuffer == null) {
                throw IllegalStateException()
            }
            return AvcConfigurationRecord(
                configurationVersion = 0x01,
                avcProfileIndication = spsBuffer[5],
                profileCompatibility = spsBuffer[6],
                avcLevelIndication = spsBuffer[7],
                lengthSizeMinusOneWithReserved = 0xFF.toByte(),
                numOfSequenceParameterSetsWithReserved = 0xE1.toByte(),
                sequenceParameterSets = ArrayList<ByteArray>(1).apply {
                    spsBuffer.position(4)
                    this.add(spsBuffer.array())
                },
                pictureParameterSets = ArrayList<ByteArray>(1).apply {
                    ppsBuffer.position(4)
                    this.add(ppsBuffer.array())
                }
            )
        }
    }
}
