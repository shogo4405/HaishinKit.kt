package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpSocket
import java.nio.ByteBuffer

/**
 * 5.4.3 Acknowledgement (3)
 */
internal class RtmpAcknowledgementMessage : RtmpMessage(RtmpMessage.Type.ACK) {
    var sequence: ByteArray? = null
        private set

    init {
        sequence = ByteArray(CAPACITY)
    }

    override fun encode(socket: RtmpSocket): ByteBuffer {
        val buffer = ByteBuffer.allocate(CAPACITY)
        buffer.put(sequence)
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        buffer.get(sequence)
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        return this
    }

    companion object {
        private const val CAPACITY = 4
    }
}
