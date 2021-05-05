package com.haishinkit.rtmp.messages

import androidx.core.util.Pools
import com.haishinkit.rtmp.RtmpObjectEncoding
import java.lang.IllegalArgumentException

internal class RtmpMessageFactory(maxPoolSize: Int) {
    private val user = Pools.SimplePool<RtmpMessage>(maxPoolSize)
    private val audio = Pools.SynchronizedPool<RtmpMessage>(maxPoolSize)
    private val video = Pools.SynchronizedPool<RtmpMessage>(maxPoolSize)

    fun create(value: Byte): RtmpMessage {
        return when (value) {
            RtmpMessage.TYPE_CHUNK_SIZE -> RtmpSetChunkSizeMessage()
            RtmpMessage.TYPE_ABORT -> RtmpAbortMessage()
            RtmpMessage.TYPE_ACK -> RtmpAcknowledgementMessage()
            RtmpMessage.TYPE_USER -> user.acquire() ?: RtmpUserControlMessage()
            RtmpMessage.TYPE_WINDOW_ACK -> RtmpWindowAcknowledgementSizeMessage()
            RtmpMessage.TYPE_BANDWIDTH -> RtmpSetPeerBandwidthMessage()
            RtmpMessage.TYPE_AUDIO -> audio.acquire() ?: RtmpAudioMessage(audio)
            RtmpMessage.TYPE_VIDEO -> video.acquire() ?: RtmpVideoMessage(video)
            RtmpMessage.TYPE_AMF0_DATA -> RtmpDataMessage(RtmpObjectEncoding.AMF0)
            RtmpMessage.TYPE_AMF0_COMMAND -> RtmpCommandMessage(RtmpObjectEncoding.AMF0)
            else -> throw IllegalArgumentException("type=$value")
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
        return (user.acquire() as? RtmpUserControlMessage) ?: RtmpUserControlMessage()
    }

    fun createRtmpWindowAcknowledgementSizeMessage(): RtmpWindowAcknowledgementSizeMessage {
        return RtmpWindowAcknowledgementSizeMessage()
    }

    fun createRtmpSetPeerBandwidthMessage(): RtmpSetPeerBandwidthMessage {
        return RtmpSetPeerBandwidthMessage()
    }

    fun createRtmpVideoMessage(): RtmpVideoMessage {
        return (video.acquire() as? RtmpVideoMessage) ?: RtmpVideoMessage()
    }

    fun createRtmpAudioMessage(): RtmpAudioMessage {
        return (audio.acquire() as? RtmpAudioMessage) ?: RtmpAudioMessage()
    }
}
