package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpSocket
import java.nio.ByteBuffer

/**
 * 5.4.1. Set Chunk Size (1)
 */
internal class RtmpSetChunkSizeMessage : RtmpMessage(RtmpMessage.Type.CHUNK_SIZE) {
    var size: Int = 0

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
        connection.socket.chunkSizeC = size
        return this
    }

    companion object {
        private const val CAPACITY = 4
    }
}
