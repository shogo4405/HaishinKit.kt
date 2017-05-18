package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPSocket

import java.nio.ByteBuffer

open internal class RTMPVideoMessage : RTMPMessage(RTMPMessage.Type.VIDEO) {
    var frame: Byte = 0x00
    var codec: Byte = 0x00
    var payload: ByteBuffer? = null

    override fun encode(socket: RTMPSocket): ByteBuffer {
        val length = payload?.limit() ?: 0
        val buffer = ByteBuffer.allocate(1 + length)
        buffer.put((frame.toInt() shl 4 or codec.toInt()).toByte())
        if (0 < length) {
            buffer.put(payload)
        }
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RTMPMessage {
        buffer.position(buffer.position() + length)
        return this
    }

    override fun execute(connection: RTMPConnection): RTMPMessage {
        return this
    }
}
