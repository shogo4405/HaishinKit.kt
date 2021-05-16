package com.haishinkit.iso

import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import com.haishinkit.codec.CodecOption
import com.haishinkit.codec.VideoCodec
import com.haishinkit.util.toPositiveInt
import org.apache.commons.lang3.builder.ToStringBuilder
import java.nio.ByteBuffer
import java.util.ArrayList

data class AvcConfigurationRecord(
    val configurationVersion: Byte = 0x01,
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

    fun encode(buffer: ByteBuffer): AvcConfigurationRecord {
        buffer.put(configurationVersion)
        buffer.put(avcProfileIndication)
        buffer.put(profileCompatibility)
        buffer.put(avcLevelIndication)
        buffer.put(lengthSizeMinusOneWithReserved)
        // SPS
        buffer.put(numOfSequenceParameterSetsWithReserved)
        sequenceParameterSets?.let {
            for (sps in it) {
                buffer.putShort(sps.size.toShort())
                buffer.put(sps)
            }
        }
        // PPS
        pictureParameterSets?.let {
            buffer.put(it.size.toByte())
            for (pps in it) {
                buffer.putShort(pps.size.toShort())
                buffer.put(pps)
            }
        }
        return this
    }

    fun decode(buffer: ByteBuffer): AvcConfigurationRecord {
        val configurationVersion = buffer.get()
        val avcProfileIndication = buffer.get()
        val profileCompatibility = buffer.get()
        val avcLevelIndication = buffer.get()
        val lengthSizeMinusOneWithReserved = buffer.get()
        val numOfSequenceParameterSetsWithReserved = buffer.get()

        val numOfSequenceParameterSets = numOfSequenceParameterSetsWithReserved.toPositiveInt() and RESERVE_NUM_OF_SEQUENCE_PARAMETER_SETS.inv()
        val sequenceParameterSets = mutableListOf<ByteArray>()
        for (i in 0 until numOfSequenceParameterSets) {
            val bytes = ByteArray(buffer.short.toInt())
            buffer.get(bytes)
            sequenceParameterSets.add(bytes)
        }

        val numPictureParameterSets = buffer.get().toPositiveInt()
        val pictureParameterSets = mutableListOf<ByteArray>()
        for (i in 0 until numPictureParameterSets) {
            val bytes = ByteArray(buffer.short.toInt())
            buffer.get(bytes)
            pictureParameterSets.add(bytes)
        }

        return AvcConfigurationRecord(
            configurationVersion = configurationVersion,
            avcProfileIndication = avcProfileIndication,
            profileCompatibility = profileCompatibility,
            avcLevelIndication = avcLevelIndication,
            lengthSizeMinusOneWithReserved = lengthSizeMinusOneWithReserved,
            numOfSequenceParameterSetsWithReserved = numOfSequenceParameterSetsWithReserved,
            sequenceParameterSets = sequenceParameterSets,
            pictureParameterSets = pictureParameterSets
        )
    }

    internal fun allocate(): ByteBuffer {
        var capacity = 5
        sequenceParameterSets?.let {
            for (sps in it) {
                capacity += 3 + sps.size
            }
        }
        pictureParameterSets?.let {
            for (psp in it) {
                capacity += 3 + psp.size
            }
        }
        return ByteBuffer.allocate(capacity)
    }

    internal fun apply(codec: VideoCodec) {
        when (avcProfileIndication.toInt()) {
            66 -> codec.profile = MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline
            77 -> codec.profile = MediaCodecInfo.CodecProfileLevel.AVCProfileMain
            88 -> codec.profile = MediaCodecInfo.CodecProfileLevel.AVCProfileHigh
        }
        when (avcLevelIndication.toInt()) {
            31 -> codec.level = MediaCodecInfo.CodecProfileLevel.AVCLevel31
            32 -> codec.level = MediaCodecInfo.CodecProfileLevel.AVCLevel32
            40 -> codec.level = MediaCodecInfo.CodecProfileLevel.AVCLevel4
            41 -> codec.level = MediaCodecInfo.CodecProfileLevel.AVCLevel41
            42 -> codec.level = MediaCodecInfo.CodecProfileLevel.AVCLevel42
            50 -> codec.level = MediaCodecInfo.CodecProfileLevel.AVCLevel5
            51 -> codec.level = MediaCodecInfo.CodecProfileLevel.AVCLevel51
            52 -> codec.level = MediaCodecInfo.CodecProfileLevel.AVCLevel52
        }
        val options = mutableListOf<CodecOption>()
        val spsBuffer = ByteBuffer.allocate(128)
        sequenceParameterSets?.let {
            for (sps in it) {
                spsBuffer.put(AvcFormatUtils.START_CODE)
                spsBuffer.put(sps)
            }
        }
        spsBuffer.flip()
        options.add(CodecOption(CSD0, spsBuffer))
        val ppsBuffer = ByteBuffer.allocate(128)
        pictureParameterSets?.let {
            for (pps in it) {
                ppsBuffer.put(AvcFormatUtils.START_CODE)
                ppsBuffer.put((pps))
            }
        }
        ppsBuffer.flip()
        options.add(CodecOption(CSD1, ppsBuffer))
        codec.options = options
        Log.i(TAG, "apply value=$this for a videoCodec")
    }

    internal fun toByteBuffer(): ByteBuffer {
        val result = ByteBuffer.allocate(128)
        result.put(0x00).put(0x00).put(0x00).put(0x00)
        sequenceParameterSets?.let {
            for (sps in it) {
                result.put(AvcFormatUtils.START_CODE)
                result.put(sps)
            }
        }
        pictureParameterSets?.let {
            for (pps in it) {
                result.put(AvcFormatUtils.START_CODE)
                result.put(pps)
            }
        }
        return result
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    @Suppress("unused")
    companion object {
        const val RESERVE_LENGTH_SIZE_MINUS_ONE = 0x3F
        const val RESERVE_NUM_OF_SEQUENCE_PARAMETER_SETS = 0xE0
        const val RESERVE_CHROME_FORMAT = 0xFC
        const val RESERVE_BIT_DEPTH_LUMA_MINUS8 = 0xF8
        const val RESERVE_BIT_DEPTH_CHROME_MINUS8 = 0xF8

        private const val CSD0 = "csd-0"
        private const val CSD1 = "csd-1"

        private var TAG = AvcConfigurationRecord::class.java.simpleName

        internal fun create(mediaFormat: MediaFormat): AvcConfigurationRecord {
            // SPS => 0x00,0x00,0x00,0x01,0x67,0x42,0x00,0x29,0x8d,0x8d,0x40,0xa0,0xfd,0x00,0xf0,0x88,0x45,0x38
            val spsBuffer = mediaFormat.getByteBuffer(CSD0)
            // PPS => 0x00,0x00,0x00,0x01,0x68,0xca,0x43,0xc8
            val ppsBuffer = mediaFormat.getByteBuffer(CSD1)
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
                    val length = spsBuffer.remaining()
                    add(spsBuffer.array().slice(4 until length).toByteArray())
                },
                pictureParameterSets = ArrayList<ByteArray>(1).apply {
                    val length = ppsBuffer.remaining()
                    add(ppsBuffer.array().slice(4 until length).toByteArray())
                }
            )
        }
    }
}
