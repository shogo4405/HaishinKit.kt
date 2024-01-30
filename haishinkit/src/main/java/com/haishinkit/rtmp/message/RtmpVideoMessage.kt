package com.haishinkit.rtmp.message

import android.media.MediaCodec
import android.util.Log
import android.util.Size
import androidx.core.util.Pools
import com.haishinkit.flv.FlvAvcPacketType
import com.haishinkit.flv.FlvFlameType
import com.haishinkit.flv.FlvVideoCodec
import com.haishinkit.iso.AvcConfigurationRecord
import com.haishinkit.iso.AvcFormatUtils
import com.haishinkit.iso.SequenceParameterSet
import com.haishinkit.media.MediaCodecSource
import com.haishinkit.rtmp.RtmpChunk
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.util.toPositiveInt
import java.nio.ByteBuffer

internal class RtmpVideoMessage(pool: Pools.Pool<RtmpMessage>? = null) :
    RtmpMessage(TYPE_VIDEO, pool) {
    var frame: Byte = 0x00
    var codec: Byte = 0x00
    var data: ByteBuffer? = null
    var packetType: Byte = 0x00
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

    override var length: Int
        get() {
            if (super.length == -1) {
                return 5 + (data?.limit() ?: 0)
            }
            return super.length
        }
        set(value) {
            super.length = value
        }

    override fun encode(buffer: ByteBuffer): RtmpMessage {
        buffer.put((frame.toInt() shl 4 or codec.toInt()).toByte())
        buffer.put(packetType)
        buffer.put((compositeTime shr 16).toByte()).put((compositeTime shr 8).toByte())
            .put(compositeTime.toByte())
        data?.let {
            when (packetType) {
                FlvAvcPacketType.NAL -> {
                    AvcFormatUtils.toNALFile(it, buffer)
                }

                else -> {
                    buffer.put(it)
                }
            }
        }
        return this
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        if (length == 0) return this
        val first = buffer.get().toPositiveInt()
        codec = (first and 0x0F).toByte()
        frame = (first shr 4).toByte()
        if (1 < length) {
            val payload = ByteArray(length - 1)
            buffer.get(payload)
            data = ByteBuffer.wrap(payload)
        }
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        if (codec != FlvVideoCodec.AVC) return this
        val stream = connection.streams[streamID] ?: return this
        data?.let { it ->
            when (val byte = it.get()) {
                FlvAvcPacketType.SEQ -> {
                    if (!it.hasRemaining()) return this
                    it.position(4)
                    val record = AvcConfigurationRecord.decode(it)

                    record.sequenceParameterSets?.let {
                        val byteArray = it.firstOrNull() ?: return@let
                        val byteBuffer = ByteBuffer.wrap(byteArray)
                        val sequenceParameterSet = SequenceParameterSet.decode(byteBuffer)
                        stream.attachVideo(
                            MediaCodecSource(
                                Size(
                                    sequenceParameterSet.videoWidth,
                                    sequenceParameterSet.videoHeight,
                                ),
                            ),
                        )
                    }

                    if (record.apply(stream.videoCodec)) {
                        data = record.toByteBuffer()
                        timestamp = 0
                        stream.muxer.hasVideo = true
                        stream.muxer.enqueueVideo(this)
                    }
                }

                FlvAvcPacketType.NAL -> {
                    if (!it.hasRemaining()) return this
                    if (chunk == RtmpChunk.ZERO) {
                        val currentTimestamp = timestamp
                        timestamp -= stream.videoTimestamp
                        stream.videoTimestamp = currentTimestamp
                    }
                    it.position(0)
                    AvcFormatUtils.toByteStream(it, 4)
                    it.position(it.position() + 4)
                    stream.muxer.enqueueVideo(this)
                }

                FlvAvcPacketType.EOS -> {
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
        if (frame == FlvFlameType.KEY) {
            return MediaCodec.BUFFER_FLAG_KEY_FRAME
        }
        return when (data?.get(0)) {
            FlvAvcPacketType.SEQ -> {
                MediaCodec.BUFFER_FLAG_CODEC_CONFIG
            }

            else -> {
                0
            }
        }
    }

    companion object {
        private val TAG = RtmpVideoMessage::class.java.simpleName
        private const val VERBOSE = false
    }
}
