package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RTMPChunk
import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPSocket
import java.nio.ByteBuffer

/**
 * 5.4.4. Window Acknowledgement Size (5)
 */
internal class RTMPWindowAcknowledgementSizeMessage : RTMPMessage(RTMPMessage.Type.ACK) {
    var size: Int = 0
        private set

    override fun encode(socket: RTMPSocket): ByteBuffer {
        val buffer = ByteBuffer.allocate(CAPACITY)
        buffer.putInt(size)
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RTMPMessage {
        size = buffer.int
        return this
    }

    override fun execute(connection: RTMPConnection): RTMPMessage {
        var ack = RTMPWindowAcknowledgementSizeMessage()
        ack.size = size
        ack.chunkStreamID = RTMPChunk.CONTROL
        connection.socket.doOutput(RTMPChunk.ZERO, ack)
        return this
    }

    companion object {
        private const val CAPACITY = 4
    }
}
