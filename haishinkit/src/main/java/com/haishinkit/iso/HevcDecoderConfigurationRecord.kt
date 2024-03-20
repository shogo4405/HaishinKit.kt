package com.haishinkit.iso

import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.haishinkit.codec.CodecOption
import com.haishinkit.codec.VideoCodec
import java.nio.ByteBuffer

/// ISO/IEC 14496-15 8.3.3.1.2
internal data class HevcDecoderConfigurationRecord(
    val configurationVersion: UByte,
    val generalProfileSpace: UByte,
    val generalTierFlag: Boolean,
    val generalProfileIdc: UByte,
    val generalProfileCompatibilityFlags: UInt,
    val generalConstraintIndicatorFlags: ULong,
    val generalLevelIdc: UByte,
    val minSpatialSegmentationIdc: UShort,
    val parallelismType: UByte,
    val chromaFormat: UByte,
    val bitDepthLumaMinus8: UByte,
    val bitDepthChromaMinus8: UByte,
    val avgFrameRate: UShort,
    val constantFrameRate: UByte,
    val numTemporalLayers: UByte,
    val temporalIdNested: Boolean,
    val lengthSizeMinusOne: UByte,
    val numberOfArrays: UByte,
    val arrays: List<NalUnit.Hevc>
) : DecoderConfigurationRecord {
    override val capacity: Int
        get() {
            var capacity = 23
            for (unit in arrays) {
                capacity += 3 + 2
                capacity += unit.payload.remaining()
            }
            return capacity
        }

    override fun encode(buffer: ByteBuffer): HevcDecoderConfigurationRecord {
        val isoTypeBuffer = IsoTypeBuffer(buffer)
        isoTypeBuffer.putUByte(configurationVersion)
        isoTypeBuffer.putUByte(
            (generalProfileSpace.toUInt() shr 6).toUByte() or (if (generalTierFlag) {
                0x20
            } else {
                0
            }).toUByte() or (generalProfileIdc)
        )
        isoTypeBuffer.putUInt(generalProfileCompatibilityFlags)
        isoTypeBuffer.putUInt48(generalConstraintIndicatorFlags)
        isoTypeBuffer.putUByte(generalLevelIdc)
        isoTypeBuffer.putUShort((RESERVED1 shr 12).toUShort() or minSpatialSegmentationIdc)
        isoTypeBuffer.putUByte((RESERVED2 shr 2).toUByte() or parallelismType)
        isoTypeBuffer.putUByte((RESERVED3 shr 2).toUByte() or chromaFormat)
        isoTypeBuffer.putUByte((RESERVED4 shr 3).toUByte() or bitDepthLumaMinus8)
        isoTypeBuffer.putUByte((RESERVED5 shr 3).toUByte() or bitDepthChromaMinus8)
        isoTypeBuffer.putUShort(avgFrameRate)
        isoTypeBuffer.putUByte(
            (constantFrameRate.toUInt() shr 6) or (numTemporalLayers.toUInt() shr 3) or (if (temporalIdNested) {
                0x4
            } else {
                0
            }).toUInt() + lengthSizeMinusOne.toUInt()
        )
        isoTypeBuffer.putUByte(arrays.size)
        return this
    }


    override fun configure(codec: VideoCodec): Boolean {
        codec.inputMimeType = MediaFormat.MIMETYPE_VIDEO_HEVC

        codec.profile = when (generalProfileIdc) {
            PROFILE_MAIN -> MediaCodecInfo.CodecProfileLevel.HEVCProfileMain
            else -> throw IllegalStateException()
        }

        when (generalLevelIdc) {
            PROFILE_MAIN -> {
                codec.level = when (generalLevelIdc) {
                    PROFILE_LEVEL_1 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel1
                    PROFILE_LEVEL_2 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel2
                    PROFILE_LEVEL_21 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel21
                    PROFILE_LEVEL_3 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel3
                    PROFILE_LEVEL_31 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel31
                    PROFILE_LEVEL_4 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel4
                    PROFILE_LEVEL_41 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel41
                    PROFILE_LEVEL_5 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel5
                    PROFILE_LEVEL_51 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel51
                    PROFILE_LEVEL_52 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel52
                    PROFILE_LEVEL_6 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel6
                    PROFILE_LEVEL_61 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel61
                    PROFILE_LEVEL_62 -> MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel62
                    else -> throw IllegalStateException()
                }
            }
        }

        return super.configure(codec)
    }

    override fun toCodecSpecificData(options: List<CodecOption>): List<CodecOption> {
        val mutableOptions = mutableListOf<CodecOption>()
        mutableOptions.addAll(options.filter { it.key != CSD0 })
        var capacity = 0
        for (unit in arrays) {
            capacity += 6 + unit.payload.remaining()
        }
        val buffer = ByteBuffer.allocate(capacity)
        for (unit in arrays) {
            buffer.put(IsoTypeBufferUtils.START_CODE)
            buffer.put((unit.type.toUInt() shl 1).toByte())
            buffer.put(unit.temporalIdPlusOne.toByte())
            buffer.put(unit.payload)
        }
        buffer.flip()
        mutableOptions.add(CodecOption(CSD0, buffer))
        return mutableOptions
    }

    companion object : DecoderConfigurationRecordFactory {
        const val NAL_UNIT_TYPE_VPS: UByte = 32u
        const val NAL_UNIT_TYPE_SPS: UByte = 33u
        const val NAL_UNIT_TYPE_PPS: UByte = 34u
        const val CSD0 = "csd-0"

        const val PROFILE_LEVEL_1: UByte = 30u
        const val PROFILE_LEVEL_2: UByte = 60u
        const val PROFILE_LEVEL_21: UByte = 63u
        const val PROFILE_LEVEL_3: UByte = 90u
        const val PROFILE_LEVEL_31: UByte = 93u
        const val PROFILE_LEVEL_4: UByte = 120u
        const val PROFILE_LEVEL_41: UByte = 123u
        const val PROFILE_LEVEL_5: UByte = 150u
        const val PROFILE_LEVEL_51: UByte = 153u
        const val PROFILE_LEVEL_52: UByte = 156u
        const val PROFILE_LEVEL_6: UByte = 180u
        const val PROFILE_LEVEL_61: UByte = 183u
        const val PROFILE_LEVEL_62: UByte = 186u

        const val PROFILE_MAIN: UByte = 1u
        const val PROFILE_MAIN10: UByte = 10u

        private const val RESERVED1 = 0xF
        private const val RESERVED2 = 0x3F
        private const val RESERVED3 = 0x3F
        private const val RESERVED4 = 0x1F
        private const val RESERVED5 = 0x1F
        private var TAG = HevcDecoderConfigurationRecord::class.java.simpleName

        fun create(buffer: ByteBuffer): HevcDecoderConfigurationRecord {
            val units: List<NalUnit.Hevc> = NalUnitReader.readHevc(buffer)
            val spsUnit = units.firstOrNull { it.type == NAL_UNIT_TYPE_SPS } ?: throw IllegalArgumentException()
            val sps = HevcSequenceParameterSet.decode(spsUnit.payload)
            return HevcDecoderConfigurationRecord(
                configurationVersion = 1u,
                generalProfileSpace = sps.profileTierLevel.generalProfileSpace,
                generalTierFlag = sps.profileTierLevel.generalTierFlag,
                generalProfileIdc = sps.profileTierLevel.generalProfileIdc,
                generalProfileCompatibilityFlags = sps.profileTierLevel.generalProfileCompatFlags,
                generalConstraintIndicatorFlags = sps.profileTierLevel.generalConstraintIndicatorFlags,
                generalLevelIdc = sps.profileTierLevel.generalLevelIdc,
                minSpatialSegmentationIdc = 0u,
                parallelismType = 0u,
                chromaFormat = sps.chromaFormatIdc,
                bitDepthLumaMinus8 = sps.bitDepthLumaMinus8,
                bitDepthChromaMinus8 = sps.bitDepthChromaMinus8,
                avgFrameRate = 0u,
                constantFrameRate = 0u,
                numTemporalLayers = 0u,
                temporalIdNested = false,
                lengthSizeMinusOne = 3u,
                numberOfArrays = units.size.toUByte(),
                arrays = units
            )
        }

        override fun create(mediaFormat: MediaFormat): HevcDecoderConfigurationRecord {
            return create(mediaFormat.getByteBuffer(CSD0) ?: throw IllegalArgumentException())
        }

        override fun decode(buffer: ByteBuffer): HevcDecoderConfigurationRecord {
            val isoTypeBuffer = IsoTypeBuffer(buffer)
            val configurationVersion = isoTypeBuffer.getUByte()
            val generalProfileSpace = isoTypeBuffer.get(2)
            val generalTierFlag = isoTypeBuffer.boolean
            val generalProfileIdc = isoTypeBuffer.get(5)
            val generalProfileCompatibilityFlags = isoTypeBuffer.getUInt()
            val generalConstraintIndicatorFlags = isoTypeBuffer.getULong(48)
            val generalLevelIdc = isoTypeBuffer.getUByte()
            val minSpatialSegmentationIdc = isoTypeBuffer.getUShort() and 0xfffu
            val parallelismType = isoTypeBuffer.getUByte() and 0x3u
            val chromaFormat = isoTypeBuffer.getUByte() and 0x3u
            val bitDepthLumaMinus8 = isoTypeBuffer.getUByte() and 0x7u
            val bitDepthChromaMinus8 = isoTypeBuffer.getUByte() and 0x7u
            val avgFrameRate = isoTypeBuffer.getUShort()
            val constantFrameRate = isoTypeBuffer.get(2)
            val numTemporalLayers = isoTypeBuffer.get(3)
            val temporalIdNested = isoTypeBuffer.boolean
            val lengthSizeMinusOne = isoTypeBuffer.get(2)
            val numberOfArrays = isoTypeBuffer.getUByte()
            val arrays = mutableListOf<NalUnit.Hevc>()
            for (i in 0 until numberOfArrays.toInt()) {
                val type = isoTypeBuffer.getUByte()
                val numNalus = isoTypeBuffer.getUShort().toInt()
                for (j in 0 until numNalus) {
                    val length = isoTypeBuffer.getUShort().toInt()
                    arrays.add(NalUnit.Hevc.create(isoTypeBuffer.getBytes(length)))
                }
            }
            return HevcDecoderConfigurationRecord(
                configurationVersion = configurationVersion,
                generalProfileSpace = generalProfileSpace,
                generalTierFlag = generalTierFlag,
                generalProfileIdc = generalProfileIdc,
                generalProfileCompatibilityFlags = generalProfileCompatibilityFlags,
                generalConstraintIndicatorFlags = generalConstraintIndicatorFlags,
                generalLevelIdc = generalLevelIdc,
                minSpatialSegmentationIdc = minSpatialSegmentationIdc,
                parallelismType = parallelismType,
                chromaFormat = chromaFormat,
                bitDepthLumaMinus8 = bitDepthLumaMinus8,
                bitDepthChromaMinus8 = bitDepthChromaMinus8,
                avgFrameRate = avgFrameRate,
                constantFrameRate = constantFrameRate,
                numTemporalLayers = numTemporalLayers,
                temporalIdNested = temporalIdNested,
                lengthSizeMinusOne = lengthSizeMinusOne,
                numberOfArrays = numberOfArrays,
                arrays = arrays
            )
        }
    }
}