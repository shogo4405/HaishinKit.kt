package com.haishinkit.rtmp.messages

import com.haishinkit.flv.VideoCodec
import com.haishinkit.rtmp.RtmpSocket
import java.nio.ByteBuffer

internal class RtmpAvcVideoMessage : RtmpVideoMessage() {
    var packetType: Byte = 0
    var compositeTime = 0

    init {
        codec = VideoCodec.AVC
    }

    override fun encode(socket: RtmpSocket): ByteBuffer {
        val length = payload?.limit() ?: 0
        val buffer = ByteBuffer.allocate(5 + length)
        buffer.put((frame.toInt() shl 4 or codec.toInt()).toByte())
        buffer.put(packetType)
        buffer.put(byteArrayOf((compositeTime shr 16).toByte(), (compositeTime shr 8).toByte(), compositeTime.toByte()))
        if (0 < length) {
            buffer.put(payload)
        }
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        buffer.position(buffer.position() + length)
        return this
    }
}
