package com.haishinkit.rtmp.message

import android.util.Log
import androidx.core.util.Pools
import com.haishinkit.flv.FlvAacPacketType
import com.haishinkit.flv.FlvAudioCodec
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.util.toPositiveInt
import java.nio.ByteBuffer

internal class RtmpAudioMessage(pool: Pools.Pool<RtmpMessage>? = null) :
    RtmpMessage(TYPE_AUDIO, pool) {
    var codec: Byte = 0
    var soundRate: Byte = 0
    var soundSize: Byte = 0
    var soundType: Byte = 0
    var data: ByteBuffer? = null
    var aacPacketType: Byte = 0
    override var length: Int
        get() {
            if (super.length == -1) {
                return 2 + (data?.limit() ?: 0)
            }
            return super.length
        }
        set(value) {
            super.length = value
        }

    override fun encode(buffer: ByteBuffer): RtmpMessage {
        if (codec == FlvAudioCodec.AAC) {
            buffer.put(AAC)
            buffer.put(aacPacketType)
        }
        data?.let {
            buffer.put(it)
        }
        return this
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        if (length == 0) return this
        val first = buffer.get().toPositiveInt()
        codec = (first shr 4).toByte()
        soundRate = (first and (12 shr 2)).toByte()
        soundSize = (first and (2 shr 1)).toByte()
        soundType = (first and 1).toByte()
        if (1 < length) {
            val payload = ByteArray(length - 1)
            buffer.get(payload)
            data = ByteBuffer.wrap(payload)
        }
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        if (codec != FlvAudioCodec.AAC) return this
        val stream = connection.streams[streamID] ?: return this
        data?.let {
            when (val byte = it.get()) {
                FlvAacPacketType.SEQ -> {
                    Log.i(TAG, "$this")
                    timestamp = 0
                    stream.muxer.hasAudio = true
                    stream.muxer.enqueueAudio(this)
                }
                FlvAacPacketType.RAW -> {
                    stream.muxer.enqueueAudio(this)
                }
                else -> {
                    if (VERBOSE) Log.d(TAG, "unknown data=$byte")
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
        return when (data?.get(0)) {
            FlvAacPacketType.SEQ -> {
                android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG
            }
            else -> {
                0
            }
        }
    }

    companion object {
        private const val VERBOSE = false
        private const val AAC = (0x0A shl 4 or (0x03 shl 2) or (0x01 shl 1) or 0x01).toByte()
        private var TAG = RtmpAudioMessage::class.java.simpleName
    }
}
