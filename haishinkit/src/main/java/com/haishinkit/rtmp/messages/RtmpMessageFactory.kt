package com.haishinkit.rtmp.messages

import android.support.v4.util.Pools
import com.haishinkit.rtmp.RtmpObjectEncoding

internal final class RtmpMessageFactory(private val maxPoolSize: Int) {
    private val user = Pools.SimplePool<RtmpUserControlMessage>(maxPoolSize)
    private val audio = Pools.SimplePool<RtmpAudioMessage>(maxPoolSize)
    private val video = Pools.SimplePool<RtmpVideoMessage>(maxPoolSize)

    fun create(value: Byte): RtmpMessage {
        return when (value) {
            RtmpMessage.TYPE_CHUNK_SIZE -> RtmpSetChunkSizeMessage()
            RtmpMessage.TYPE_ABORT -> RtmpAbortMessage()
            RtmpMessage.TYPE_ACK -> RtmpAcknowledgementMessage()
            RtmpMessage.TYPE_USER -> user.acquire() ?: RtmpUserControlMessage()
            RtmpMessage.TYPE_WINDOW_ACK -> RtmpWindowAcknowledgementSizeMessage()
            RtmpMessage.TYPE_BANDWIDTH -> RtmpSetPeerBandwidthMessage()
            RtmpMessage.TYPE_AUDIO -> audio.acquire() ?: RtmpAudioMessage()
            RtmpMessage.TYPE_VIDEO -> video.acquire() ?: RtmpVideoMessage()
            RtmpMessage.TYPE_AMF0_DATA -> RtmpDataMessage(RtmpObjectEncoding.AMF0)
            RtmpMessage.TYPE_AMF0_COMMAND -> RtmpCommandMessage(RtmpObjectEncoding.AMF0)
            else -> RtmpMessage(RtmpMessage.TYPE_UNKNOWN)
        }
    }

    fun release(message: RtmpMessage) {
        when (message) {
            is RtmpUserControlMessage -> user.release(message)
            is RtmpAudioMessage -> audio.release(message)
            is RtmpVideoMessage -> video.release(message)
        }
    }

    fun createRtmpSetChunkSizeMessage(): RtmpSetChunkSizeMessage {
        return RtmpSetChunkSizeMessage()
    }

    fun createRtmpAbortMessage(): RtmpAbortMessage {
        return RtmpAbortMessage()
    }

    fun createRtmpAcknowledgementMessage(): RtmpAcknowledgementMessage {
        return RtmpAcknowledgementMessage()
    }

    fun createRtmpUserControlMessage(): RtmpUserControlMessage {
        return user.acquire() ?: RtmpUserControlMessage()
    }

    fun createRtmpWindowAcknowledgementSizeMessage(): RtmpWindowAcknowledgementSizeMessage {
        return RtmpWindowAcknowledgementSizeMessage()
    }

    fun createRtmpSetPeerBandwidthMessage(): RtmpSetPeerBandwidthMessage {
        return RtmpSetPeerBandwidthMessage()
    }

    fun createRtmpVideoMessage(): RtmpVideoMessage {
        return video.acquire() ?: RtmpAvcVideoMessage()
    }

    fun createRtmpAudioMessage(): RtmpAudioMessage {
        return audio.acquire() ?: RtmpAacAudioMessage()
    }
}
