package com.haishinkit.rtmp.messages

import android.support.v4.util.Pools
import com.haishinkit.rtmp.RtmpObjectEncoding

internal final class RtmpMessageFactory(private val maxPoolSize: Int) {
    private val user = Pools.SimplePool<RtmpUserControlMessage>(maxPoolSize)
    private val audio = Pools.SimplePool<RtmpAudioMessage>(maxPoolSize)
    private val video = Pools.SimplePool<RtmpVideoMessage>(maxPoolSize)

    fun create(value: Byte): RtmpMessage {
        return when (value) {
            RtmpMessage.Type.CHUNK_SIZE.rawValue -> RtmpSetChunkSizeMessage()
            RtmpMessage.Type.ABORT.rawValue -> RtmpAbortMessage()
            RtmpMessage.Type.ACK.rawValue -> RtmpAcknowledgementMessage()
            RtmpMessage.Type.USER.rawValue -> user.acquire() ?: RtmpUserControlMessage()
            RtmpMessage.Type.WINDOW_ACK.rawValue -> RtmpWindowAcknowledgementSizeMessage()
            RtmpMessage.Type.BANDWIDTH.rawValue -> RtmpSetPeerBandwidthMessage()
            RtmpMessage.Type.AUDIO.rawValue -> audio.acquire() ?: RtmpAudioMessage()
            RtmpMessage.Type.VIDEO.rawValue -> video.acquire() ?: RtmpVideoMessage()
            RtmpMessage.Type.AMF0_DATA.rawValue -> RtmpDataMessage(RtmpObjectEncoding.AMF0)
            RtmpMessage.Type.AMF0_COMMAND.rawValue -> RtmpCommandMessage(RtmpObjectEncoding.AMF0)
            else -> RtmpMessage(RtmpMessage.Type.UNKNOWN)
        }
    }

    fun release(message: RtmpMessage) {
        when (message) {
            is RtmpUserControlMessage -> user.release(message)
            is RtmpAudioMessage -> audio.release(message)
            is RtmpVideoMessage -> video.release(message)
        }
    }

    fun createRTMPSetChunkSizeMessage(): RtmpSetChunkSizeMessage {
        return RtmpSetChunkSizeMessage()
    }

    fun createRTMPAbortMessage(): RtmpAbortMessage {
        return RtmpAbortMessage()
    }

    fun createRTMPAcknowledgementMessage(): RtmpAcknowledgementMessage {
        return RtmpAcknowledgementMessage()
    }

    fun createRTMPUserControlMessage(): RtmpUserControlMessage {
        return user.acquire() ?: RtmpUserControlMessage()
    }

    fun createRTMPWindowAcknowledgementSizeMessage(): RtmpWindowAcknowledgementSizeMessage {
        return RtmpWindowAcknowledgementSizeMessage()
    }

    fun createRTMPSetPeerBandwidthMessage(): RtmpSetPeerBandwidthMessage {
        return RtmpSetPeerBandwidthMessage()
    }

    fun createRTMPVideoMessage(): RtmpVideoMessage {
        return video.acquire() ?: RtmpAvcVideoMessage()
    }

    fun createRTMPAudioMessage(): RtmpAudioMessage {
        return audio.acquire() ?: RtmpAacAudioMessage()
    }
}
