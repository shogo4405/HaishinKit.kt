package com.haishinkit.rtmp.message

import android.media.MediaCodec
import android.util.Log
import android.util.Size
import androidx.core.util.Pools
import com.haishinkit.codec.Codec
import com.haishinkit.codec.CodecCapabilities
import com.haishinkit.iso.AvcDecoderConfigurationRecord
import com.haishinkit.iso.AvcSequenceParameterSet
import com.haishinkit.iso.DecoderConfigurationRecord
import com.haishinkit.iso.IsoTypeBufferUtils
import com.haishinkit.media.MediaCodecSource
import com.haishinkit.rtmp.RtmpChunk
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpMuxer
import com.haishinkit.util.toPositiveInt
import java.nio.ByteBuffer

internal class RtmpVideoMessage(pool: Pools.Pool<RtmpMessage>? = null) : RtmpMessage(TYPE_VIDEO, pool) {
    var isExHeader = false
    var frame: Byte = 0x00
    var codec: Byte = 0x00
    var data: ByteBuffer? = null
    var packetType: Byte = 0x00
    var fourCC: Int = 0
    var compositeTime: Int = -1
        get() {
            if (field == -1) {
                val data = data ?: return 0
                data.position(1)
                val first = data.get().toInt()
                val second = data.get().toPositiveInt()
                val third = data.get().toPositiveInt()
                return (((first shl 24) or (second shl 16) or (third shl 8)) shr 8)
            }
            return field
        }

    private val headerSize: Int
        get() = 5 + if (isExHeader && fourCC == RtmpMuxer.FLV_VIDEO_FOUR_CC_HEVC && packetType == RtmpMuxer.FLV_VIDEO_PACKET_TYPE_CODED_FRAMES) {
            3
        } else {
            0
        }

    override var length: Int
        get() {
            if (super.length == -1) {
                return if (isExHeader) {
                    headerSize + (data?.limit() ?: 0)
                } else {
                    5 + (data?.limit() ?: 0)
                }
            }
            return super.length
        }
        set(value) {
            super.length = value
        }

    override fun encode(buffer: ByteBuffer): RtmpMessage {
        if (isExHeader) {
            buffer.put((frame.toInt() shl 4 or packetType.toInt() or 0b10000000).toByte())
            buffer.putInt(fourCC)
            if (fourCC == RtmpMuxer.FLV_VIDEO_FOUR_CC_HEVC && packetType == RtmpMuxer.FLV_VIDEO_PACKET_TYPE_CODED_FRAMES) {
                buffer.put((compositeTime shr 16).toByte()).put((compositeTime shr 8).toByte()).put(compositeTime.toByte())
            }
            data?.let {
                when (fourCC) {
                    RtmpMuxer.FLV_VIDEO_FOUR_CC_HEVC -> {
                        IsoTypeBufferUtils.toNALFile(it, buffer)
                    }

                    else -> {
                        buffer.put(it)
                    }
                }
            }
        } else {
            buffer.put((frame.toInt() shl 4 or codec.toInt()).toByte())
            buffer.put(packetType)
            buffer.put((compositeTime shr 16).toByte()).put((compositeTime shr 8).toByte()).put(compositeTime.toByte())
            data?.let {
                when (packetType) {
                    RtmpMuxer.FLV_AVC_PACKET_TYPE_NAL -> {
                        IsoTypeBufferUtils.toNALFile(it, buffer)
                    }

                    else -> {
                        buffer.put(it)
                    }
                }
            }
        }
        return this
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        if (length == 0) return this
        val first = buffer.get().toUInt()
        isExHeader = (first and 0b10000000u) != 0u
        if (isExHeader) {
            var head = 5
            frame = ((first shr 4) and 0b00000111u).toByte()
            packetType = (first and 0b00001111u).toByte()
            fourCC = buffer.getInt()
            if (fourCC == RtmpMuxer.FLV_VIDEO_FOUR_CC_HEVC && packetType == RtmpMuxer.FLV_VIDEO_PACKET_TYPE_CODED_FRAMES) {
                head += 3
                buffer.get()
                buffer.get()
                buffer.get()
            }
            if (head < length) {
                val payload = ByteArray(length - head)
                buffer.get(payload)
                data = ByteBuffer.wrap(payload)
            }
        } else {
            codec = (first and 0x0Fu).toByte()
            frame = (first shr 4).toByte()
            if (1 < length) {
                val payload = ByteArray(length - 1)
                buffer.get(payload)
                data = ByteBuffer.wrap(payload)
            }
        }
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        val payload = data ?: return this
        val stream = connection.streams[streamID] ?: return this
        if (!payload.hasRemaining()) return this

        if (isExHeader) {
            val mime = RtmpMuxer.getTypeByVideoFourCC(fourCC) ?: return this
            if (!RtmpMuxer.isSupportedVideoFourCC(fourCC)) return this
            if (!CodecCapabilities.isCodecSupportedByType(Codec.MODE_DECODE, mime)) {
                return this
            }
            when (packetType) {
                RtmpMuxer.FLV_VIDEO_PACKET_TYPE_SEQUENCE_START -> {
                    val record = DecoderConfigurationRecord.decode(mime, payload) ?: return this
                    if (record.configure(stream.videoCodec)) {
                        timestamp = 0
                        stream.muxer.hasVideo = true
                        stream.muxer.enqueueVideo(this)
                    }
                }

                RtmpMuxer.FLV_VIDEO_PACKET_TYPE_CODED_FRAMES, RtmpMuxer.FLV_VIDEO_PACKET_TYPE_CODED_FRAMES_X -> {
                    if (chunk == RtmpChunk.ZERO) {
                        val currentTimestamp = timestamp
                        timestamp -= stream.videoTimestamp
                        stream.videoTimestamp = currentTimestamp
                    }
                    if (fourCC == RtmpMuxer.FLV_VIDEO_FOUR_CC_HEVC) {
                        IsoTypeBufferUtils.toByteStream(payload, 0)
                    }
                    stream.muxer.enqueueVideo(this)
                }

                else -> {
                    if (VERBOSE) Log.d(TAG, "code=$packetType")
                }
            }
        } else {
            if (codec != RtmpMuxer.FLV_VIDEO_CODEC_AVC) return this

            when (val byte = payload.get()) {
                RtmpMuxer.FLV_AVC_PACKET_TYPE_SEQ -> {
                    payload.position(4)
                    val record = AvcDecoderConfigurationRecord.decode(payload)

                    record.sequenceParameterSets?.let {
                        val byteArray = it.firstOrNull() ?: return@let
                        val byteBuffer = ByteBuffer.wrap(byteArray)
                        val sequenceParameterSet = AvcSequenceParameterSet.decode(byteBuffer)
                        stream.attachVideo(
                            MediaCodecSource(
                                Size(
                                    sequenceParameterSet.videoWidth,
                                    sequenceParameterSet.videoHeight,
                                ),
                            ),
                        )
                    }

                    if (record.configure(stream.videoCodec)) {
                        data = record.toByteBuffer()
                        timestamp = 0
                        stream.muxer.hasVideo = true
                        stream.muxer.enqueueVideo(this)
                    }
                }

                RtmpMuxer.FLV_AVC_PACKET_TYPE_NAL -> {
                    if (chunk == RtmpChunk.ZERO) {
                        val currentTimestamp = timestamp
                        timestamp -= stream.videoTimestamp
                        stream.videoTimestamp = currentTimestamp
                    }
                    payload.position(0)
                    IsoTypeBufferUtils.toByteStream(payload, 4)
                    payload.position(payload.position() + 4)
                    stream.muxer.enqueueVideo(this)
                }

                RtmpMuxer.FLV_AVC_PACKET_TYPE_EOS -> {

                }

                else -> {
                    if (VERBOSE) Log.d(TAG, "code=$byte")
                }
            }
        }
        return this
    }

    override fun release(): Boolean {
        data = null
        payload.clear()
        return super.release()
    }

    fun toFlags(): Int {
        if (isExHeader) {
            if (packetType == RtmpMuxer.FLV_VIDEO_PACKET_TYPE_SEQUENCE_START) {
                return MediaCodec.BUFFER_FLAG_CODEC_CONFIG
            }
            return when (frame) {
                RtmpMuxer.FLV_FRAME_TYPE_KEY -> {
                    return MediaCodec.BUFFER_FLAG_KEY_FRAME
                }

                else -> {
                    0
                }
            }
        } else {
            if (frame == RtmpMuxer.FLV_FRAME_TYPE_KEY) {
                return MediaCodec.BUFFER_FLAG_KEY_FRAME
            }
            return when (data?.get(0)) {
                RtmpMuxer.FLV_AVC_PACKET_TYPE_SEQ -> {
                    MediaCodec.BUFFER_FLAG_CODEC_CONFIG
                }

                else -> {
                    0
                }
            }
        }
    }

    companion object {
        private val TAG = RtmpVideoMessage::class.java.simpleName
        private const val VERBOSE = false
    }
}
