package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpSocket
import java.nio.ByteBuffer

/**
 * 5.4.2. Abort Message (2)
 */
internal class RtmpAbortMessage : RtmpMessage(RtmpMessage.Type.ABORT) {
    var discarded: Int = 0
        private set

    override fun encode(socket: RtmpSocket): ByteBuffer {
        val buffer = ByteBuffer.allocate(CAPACITY)
        buffer.putInt(discarded)
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        discarded = buffer.int
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        return this
    }

    companion object {
        private const val CAPACITY = 4
    }
}
