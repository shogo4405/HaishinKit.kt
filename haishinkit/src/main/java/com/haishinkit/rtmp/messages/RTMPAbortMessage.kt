package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPSocket

import java.nio.ByteBuffer

/**
 * 5.4.2. Abort Message (2)
 */
internal class RTMPAbortMessage : RTMPMessage(RTMPMessage.Type.ABORT) {
    var discarded: Int = 0
        private set

    override fun encode(socket: RTMPSocket): ByteBuffer {
        val buffer = ByteBuffer.allocate(CAPACITY)
        buffer.putInt(discarded)
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RTMPMessage {
        discarded = buffer.int
        return this
    }

    override fun execute(connection: RTMPConnection): RTMPMessage {
        return this
    }

    companion object {
        private val CAPACITY = 4
    }
}
