package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RtmpConnection
import org.apache.commons.lang3.NotImplementedException
import org.apache.commons.lang3.builder.ToStringBuilder
import java.nio.ByteBuffer

internal open class RtmpMessage(val type: Type) {
    enum class Type(val rawValue: Byte) {
        CHUNK_SIZE(0x01),
        ABORT(0x02),
        ACK(0x03),
        USER(0x04),
        WINDOW_ACK(0x05),
        BANDWIDTH(0x06),
        AUDIO(0x08),
        VIDEO(0x09),
        AMF3_DATA(0x0F),
        AMF3_SHARED(0x10),
        AMF3_COMMAND(0x11),
        AMF0_DATA(0x12),
        AMF0_SHARED(0x13),
        AMF0_COMMAND(0x14),
        AGGREGATE(0x16),
        UNKNOWN(Byte.MAX_VALUE);
    }

    var chunkStreamID: Short = 0
    var streamID: Int = 0
    var timestamp: Int = 0
    open var payload: ByteBuffer = EMPTY_BYTE_BUFFER
        get() {
            if (field.capacity() < length) {
                field = ByteBuffer.allocate(length)
            } else {
                field.clear()
            }
            return field
        }
    open var length: Int = 0

    open fun encode(buffer: ByteBuffer): RtmpMessage {
        throw NotImplementedException("$TAG#encode")
    }

    open fun decode(buffer: ByteBuffer): RtmpMessage {
        throw NotImplementedException("$TAG#decode")
    }

    open fun execute(connection: RtmpConnection): RtmpMessage {
        throw NotImplementedException("$TAG#execute")
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        val EMPTY_BYTE_BUFFER: ByteBuffer = ByteBuffer.allocate(0)
        private val TAG = RtmpMessage::class.java.simpleName
    }
}
