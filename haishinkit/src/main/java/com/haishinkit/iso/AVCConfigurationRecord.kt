package com.haishinkit.iso

import android.media.MediaFormat
import org.apache.commons.lang3.builder.ToStringBuilder
import java.nio.ByteBuffer
import java.util.ArrayList

class AVCConfigurationRecord {

    var configurationVersion: Byte = 1
    var avcProfileIndication: Byte = 0
    var profileCompatibility: Byte = 0
    var avcLevelIndication: Byte = 0
    var lengthSizeMinusOneWithReserved: Byte = 0
    var numOfSequenceParameterSetsWithReserved: Byte = 0
    var sequenceParameterSets: List<ByteArray>? = null
    var pictureParameterSets: List<ByteArray>? = null

    constructor() {}

    constructor(mediaFormat: MediaFormat) {
        // SPS => 0x00,0x00,0x00,0x01,0x67,0x42,0x00,0x29,0x8d,0x8d,0x40,0xa0,0xfd,0x00,0xf0,0x88,0x45,0x38
        val spsBuffer = mediaFormat.getByteBuffer("csd-0")
        // PPS => 0x00,0x00,0x00,0x01,0x68,0xca,0x43,0xc8
        val ppsBuffer = mediaFormat.getByteBuffer("csd-1")

        if (spsBuffer == null || ppsBuffer == null) {
            throw IllegalStateException()
        }

        /**
         * SPS layout

         * profile_idc (8)
         * constraint_set0_flag (1)
         * constraint_set1_flag (1)
         * constraint_set2_flag (1)
         * constraint_set3_flag (1)
         * constraint_set4_flag (1)
         * reserved_zero_3bits (3)
         * level_idc (8)
         * ...
         */
        spsBuffer.position(4)
        val spsBytes = ByteArray(spsBuffer.remaining())
        spsBuffer.get(spsBytes)
        configurationVersion = 0x01
        avcProfileIndication = spsBytes[1]
        profileCompatibility = spsBytes[2]
        avcLevelIndication = spsBytes[3]
        lengthSizeMinusOneWithReserved = 0xFF.toByte()

        // SPS
        numOfSequenceParameterSetsWithReserved = 0xE1.toByte()
        val spsList = ArrayList<ByteArray>(1)
        spsList.add(spsBytes)
        sequenceParameterSets = spsList

        // PPS
        ppsBuffer.position(4)
        val ppsBytes = ByteArray(ppsBuffer.remaining())
        ppsBuffer.get(ppsBytes)
        val ppsList = ArrayList<ByteArray>(1)
        ppsList.add(ppsBytes)
        pictureParameterSets = ppsList
    }

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
    }
}
