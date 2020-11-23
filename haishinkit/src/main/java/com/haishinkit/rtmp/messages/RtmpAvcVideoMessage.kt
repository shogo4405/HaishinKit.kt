package com.haishinkit.rtmp.messages

import com.haishinkit.flv.FlvAvcPacketType
import com.haishinkit.flv.FlvVideoCodec
import com.haishinkit.iso.AvcFormatUtils
import java.nio.ByteBuffer

internal class RtmpAvcVideoMessage : RtmpVideoMessage() {
    override var codec = FlvVideoCodec.AVC
    var packetType: Byte = 0
    var compositeTime = 0
    override var length: Int
        get() = 5 + (data?.limit() ?: 0)
        set(value) { super.length = value }
    override var payload: ByteBuffer = EMPTY_BYTE_BUFFER
        get() {
            if (field.capacity() < length) {
                field = ByteBuffer.allocate(5 + (data?.capacity() ?: 0))
            } else {
                field.clear()
            }
            return field
        }

    override fun encode(buffer: ByteBuffer): RtmpMessage {
        buffer.put((frame.toInt() shl 4 or codec.toInt()).toByte())
        buffer.put(packetType)
        buffer.put((compositeTime shr 16).toByte()).put((compositeTime shr 8).toByte()).put(compositeTime.toByte())
        data?.let {
            when (packetType) {
                FlvAvcPacketType.NAL -> {
                    AvcFormatUtils.put(it, buffer)
                }
                else -> {
                    buffer.put(it)
                }
            }
        }
        return this
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        buffer.position(buffer.position() + length)
        return this
    }
}
