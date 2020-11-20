package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RtmpChunk
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpSocket
import java.nio.ByteBuffer

/**
 * 5.4.4. Window Acknowledgement Size (5)
 */
internal class RtmpWindowAcknowledgementSizeMessage : RtmpMessage(RtmpMessage.Type.ACK) {
    var size: Int = 0
        private set

    override fun encode(socket: RtmpSocket): ByteBuffer {
        val buffer = ByteBuffer.allocate(CAPACITY)
        buffer.putInt(size)
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        size = buffer.int
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        var ack = connection.messageFactory.createRtmpWindowAcknowledgementSizeMessage()
        ack.size = size
        ack.chunkStreamID = RtmpChunk.CONTROL
        connection.doOutput(RtmpChunk.ZERO, ack)
        return this
    }

    companion object {
        private const val CAPACITY = 4
    }
}
