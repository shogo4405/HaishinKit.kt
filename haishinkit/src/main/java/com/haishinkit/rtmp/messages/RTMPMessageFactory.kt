package com.haishinkit.rtmp.messages

import android.support.v4.util.Pools
import com.haishinkit.rtmp.RTMPObjectEncoding

internal final class RTMPMessageFactory(
    private val maxPoolSize: Int,
    private val user: Pools.SimplePool<RTMPUserControlMessage> = Pools.SimplePool<RTMPUserControlMessage>(maxPoolSize),
    private val audio: Pools.SimplePool<RTMPAudioMessage> = Pools.SimplePool<RTMPAudioMessage>(maxPoolSize),
    private val video: Pools.SimplePool<RTMPVideoMessage> = Pools.SimplePool<RTMPVideoMessage>(maxPoolSize)
) {
    fun create(value: Byte): RTMPMessage {
        return when (value) {
            RTMPMessage.Type.CHUNK_SIZE.rawValue -> RTMPSetChunkSizeMessage()
            RTMPMessage.Type.ABORT.rawValue -> RTMPAbortMessage()
            RTMPMessage.Type.ACK.rawValue -> RTMPAcknowledgementMessage()
            RTMPMessage.Type.USER.rawValue -> user.acquire() ?: RTMPUserControlMessage()
            RTMPMessage.Type.WINDOW_ACK.rawValue -> RTMPWindowAcknowledgementSizeMessage()
            RTMPMessage.Type.BANDWIDTH.rawValue -> RTMPSetPeerBandwidthMessage()
            RTMPMessage.Type.AUDIO.rawValue -> audio.acquire() ?: RTMPAudioMessage()
            RTMPMessage.Type.VIDEO.rawValue -> video.acquire() ?: RTMPVideoMessage()
            RTMPMessage.Type.AMF0_DATA.rawValue -> RTMPDataMessage(RTMPObjectEncoding.AMF0)
            RTMPMessage.Type.AMF0_COMMAND.rawValue -> RTMPCommandMessage(RTMPObjectEncoding.AMF0)
            else -> RTMPMessage(RTMPMessage.Type.UNKNOWN)
        }
    }

    fun release(message: RTMPMessage) {
        when (message) {
            is RTMPUserControlMessage -> user.release(message)
            is RTMPAudioMessage -> audio.release(message)
            is RTMPVideoMessage -> video.release(message)
        }
    }

    fun createRTMPSetChunkSizeMessage(): RTMPSetChunkSizeMessage {
        return RTMPSetChunkSizeMessage()
    }

    fun createRTMPAbortMessage(): RTMPAbortMessage {
        return RTMPAbortMessage()
    }

    fun createRTMPAcknowledgementMessage(): RTMPAcknowledgementMessage {
        return RTMPAcknowledgementMessage()
    }

    fun createRTMPUserControlMessage(): RTMPUserControlMessage {
        return user.acquire() ?: RTMPUserControlMessage()
    }

    fun createRTMPWindowAcknowledgementSizeMessage(): RTMPWindowAcknowledgementSizeMessage {
        return RTMPWindowAcknowledgementSizeMessage()
    }

    fun createRTMPSetPeerBandwidthMessage(): RTMPSetPeerBandwidthMessage {
        return RTMPSetPeerBandwidthMessage()
    }

    fun createRTMPVideoMessage(): RTMPVideoMessage {
        return video.acquire() ?: RTMPAVCVideoMessage()
    }

    fun createRTMPAudioMessage(): RTMPAudioMessage {
        return audio.acquire() ?: RTMPAACAudioMessage()
    }
}
