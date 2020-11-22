package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RtmpConnection
import java.nio.ByteBuffer

internal open class RtmpVideoMessage : RtmpMessage(RtmpMessage.Type.VIDEO) {
    var frame: Byte = 0x00
    open var codec: Byte = 0x00
    var data: ByteBuffer? = null

    override var length: Int
        get() = 1 + (data?.limit() ?: 0)
        set(value) { super.length = value }

    override fun encode(buffer: ByteBuffer): RtmpMessage {
        buffer.put((frame.toInt() shl 4 or codec.toInt()).toByte())
        data?.let {
            buffer.put(it)
        }
        return this
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        buffer.position(buffer.position() + length)
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        return this
    }
}
