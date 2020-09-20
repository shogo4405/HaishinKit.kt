package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPObjectEncoding
import com.haishinkit.rtmp.RTMPSocket
import org.apache.commons.lang3.NotImplementedException
import org.apache.commons.lang3.builder.ToStringBuilder
import java.nio.ByteBuffer

internal open class RTMPMessage(val type: Type) {
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
    var length: Int = 0

    open fun encode(socket: RTMPSocket): ByteBuffer {
        throw NotImplementedException(javaClass.name + "#encode")
    }

    open fun decode(buffer: ByteBuffer): RTMPMessage {
        throw NotImplementedException(javaClass.name + "#decode")
    }

    open fun execute(connection: RTMPConnection): RTMPMessage {
        throw NotImplementedException(javaClass.name + "#execute")
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        fun create(value: Byte): RTMPMessage {
            return when (value) {
                Type.CHUNK_SIZE.rawValue -> RTMPSetChunkSizeMessage()
                Type.ABORT.rawValue -> RTMPAbortMessage()
                Type.ACK.rawValue -> RTMPAcknowledgementMessage()
                Type.USER.rawValue -> RTMPUserControlMessage()
                Type.WINDOW_ACK.rawValue -> RTMPWindowAcknowledgementSizeMessage()
                Type.BANDWIDTH.rawValue -> RTMPSetPeerBandwidthMessage()
                Type.AUDIO.rawValue -> RTMPAudioMessage()
                Type.VIDEO.rawValue -> RTMPVideoMessage()
                Type.AMF0_DATA.rawValue -> RTMPDataMessage(RTMPObjectEncoding.AMF0)
                Type.AMF0_COMMAND.rawValue -> RTMPCommandMessage(RTMPObjectEncoding.AMF0)
                else -> RTMPMessage(Type.UNKNOWN)
            }
        }
    }
}
