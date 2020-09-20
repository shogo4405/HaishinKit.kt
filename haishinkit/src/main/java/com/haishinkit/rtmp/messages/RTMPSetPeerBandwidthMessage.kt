package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPSocket
import java.nio.ByteBuffer

/**
 * 5.4.5. Set Peer Bandwidth (6)
 */
internal class RTMPSetPeerBandwidthMessage : RTMPMessage(RTMPMessage.Type.BANDWIDTH) {
    enum class Limit(val rawValue: Byte) {
        HARD(0x00),
        SOFT(0x01),
        DYNAMIC(0x02),
        UNKNOWN(Byte.MAX_VALUE);
    }

    var size = 0
        private set
    var limit = Limit.HARD
        private set

    override fun encode(socket: RTMPSocket): ByteBuffer {
        val buffer = ByteBuffer.allocate(CAPACITY)
        buffer.putInt(size)
        buffer.put(limit.rawValue)
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RTMPMessage {
        size = buffer.int
        var limit = buffer.get()
        this.limit = Limit.values().first { n -> n.rawValue == limit }
        return this
    }

    override fun execute(connection: RTMPConnection): RTMPMessage {
        connection.socket.bandWidth = size
        return this
    }

    companion object {
        private const val CAPACITY = 5
    }
}
