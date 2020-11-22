package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RtmpConnection
import java.nio.ByteBuffer

/**
 * 5.4.5. Set Peer Bandwidth (6)
 */
internal class RtmpSetPeerBandwidthMessage : RtmpMessage(RtmpMessage.Type.BANDWIDTH) {
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
    override var length: Int = CAPACITY

    override fun encode(buffer: ByteBuffer): RtmpMessage {
        buffer.putInt(size)
        buffer.put(limit.rawValue)
        return this
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        size = buffer.int
        val limit = buffer.get()
        this.limit = Limit.values().first { n -> n.rawValue == limit }
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        connection.socket.bandWidth = size
        return this
    }

    companion object {
        private const val CAPACITY = 5
    }
}
