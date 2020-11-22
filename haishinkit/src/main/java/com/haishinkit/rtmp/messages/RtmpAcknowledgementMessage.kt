package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RtmpConnection
import java.nio.ByteBuffer

/**
 * 5.4.3 Acknowledgement (3)
 */
internal class RtmpAcknowledgementMessage : RtmpMessage(RtmpMessage.Type.ACK) {
    var sequence: ByteArray? = null
        private set
    override var length: Int = CAPACITY

    init {
        sequence = ByteArray(CAPACITY)
    }

    override fun encode(buffer: ByteBuffer): RtmpMessage {
        buffer.put(sequence)
        return this
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
