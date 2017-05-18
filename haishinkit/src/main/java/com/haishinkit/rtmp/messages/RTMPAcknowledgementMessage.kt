package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPSocket
import java.nio.ByteBuffer

/**
 * 5.4.3 Acknowledgement (3)
 */
internal class RTMPAcknowledgementMessage : RTMPMessage(RTMPMessage.Type.ACK) {
    var sequence: ByteArray? = null
        private set

    init {
        sequence = ByteArray(CAPACITY)
    }

    override fun encode(socket: RTMPSocket): ByteBuffer {
        val buffer = ByteBuffer.allocate(CAPACITY)
        buffer.put(sequence)
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RTMPMessage {
        buffer.get(sequence)
        return this
    }

    override fun execute(connection: RTMPConnection): RTMPMessage {
        return this
    }

    companion object {
        private val CAPACITY = 4
    }
}
