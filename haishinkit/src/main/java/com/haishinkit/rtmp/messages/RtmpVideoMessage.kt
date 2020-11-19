package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpSocket
import java.nio.ByteBuffer

internal open class RtmpVideoMessage : RtmpMessage(RtmpMessage.Type.VIDEO) {
    var frame: Byte = 0x00
    var codec: Byte = 0x00
    var payload: ByteBuffer? = null

    override fun encode(socket: RtmpSocket): ByteBuffer {
        val length = payload?.limit() ?: 0
        val buffer = ByteBuffer.allocate(1 + length)
        buffer.put((frame.toInt() shl 4 or codec.toInt()).toByte())
        if (0 < length) {
            buffer.put(payload)
        }
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        buffer.position(buffer.position() + length)
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        return this
    }
}
