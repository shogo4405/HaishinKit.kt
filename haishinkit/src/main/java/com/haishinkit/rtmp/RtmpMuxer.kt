package com.haishinkit.rtmp

import android.media.MediaFormat
import com.haishinkit.codec.MediaCodec
import com.haishinkit.flv.FlvAacPacketType
import com.haishinkit.flv.FlvAvcPacketType
import com.haishinkit.flv.FlvFlameType
import com.haishinkit.flv.FlvVideoCodec
import com.haishinkit.iso.AudioSpecificConfig
import com.haishinkit.iso.AvcConfigurationRecord
import com.haishinkit.rtmp.messages.RtmpAacAudioMessage
import com.haishinkit.rtmp.messages.RtmpAvcVideoMessage
import com.haishinkit.rtmp.messages.RtmpMessage
import java.nio.ByteBuffer

internal class RtmpMuxer(private val stream: RtmpStream) : MediaCodec.Listener {
    private var audioTimestamp = 0L
    private var audioConfig: AudioSpecificConfig? = null
    private var videoTimestamp = 0L
    private var videoConfig: AvcConfigurationRecord? = null

    override fun onFormatChanged(mime: String, mediaFormat: MediaFormat) {
        var message: RtmpMessage? = null
        when (mime) {
            MediaCodec.MIME_VIDEO_AVC -> {
                videoConfig = AvcConfigurationRecord.create(mediaFormat)
                val video = stream.connection.messageFactory.createRtmpVideoMessage() as RtmpAvcVideoMessage
                video.packetType = FlvAvcPacketType.SEQ
                video.frame = FlvFlameType.KEY
                video.codec = FlvVideoCodec.AVC
                video.data = videoConfig?.toByteBuffer()
                video.chunkStreamID = RtmpChunk.VIDEO
                video.streamID = stream.id
                message = video
            }
            MediaCodec.MIME_AUDIO_MP4A -> {
                val buffer = mediaFormat.getByteBuffer("csd-0") ?: return
                audioConfig = AudioSpecificConfig.create(buffer)
                val audio = stream.connection.messageFactory.createRtmpAudioMessage() as RtmpAacAudioMessage
                audio.config = audioConfig
                audio.aacPacketType = FlvAacPacketType.SEQ
                audio.data = buffer
                audio.chunkStreamID = RtmpChunk.AUDIO
                audio.streamID = stream.id
                message = audio
            }
        }
        if (message != null) {
            stream.connection.doOutput(RtmpChunk.ZERO, message)
        }
    }

    override fun onSampleOutput(mime: String, info: android.media.MediaCodec.BufferInfo, buffer: ByteBuffer) {
        if (info.flags and android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
            return
        }
        val timestamp: Int
        var message: RtmpMessage? = null
        when (mime) {
            MediaCodec.MIME_VIDEO_AVC -> {
                timestamp = (info.presentationTimeUs - videoTimestamp).toInt()
                val keyframe = info.flags and android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME != 0
                val video = stream.connection.messageFactory.createRtmpVideoMessage() as RtmpAvcVideoMessage
                video.packetType = FlvAvcPacketType.NAL
                video.frame = if (keyframe) FlvFlameType.KEY else FlvFlameType.INTER
                video.codec = FlvVideoCodec.AVC
                video.data = buffer
                video.chunkStreamID = RtmpChunk.VIDEO
                video.timestamp = timestamp / 1000
                video.streamID = stream.id
                message = video
                stream.frameCount.incrementAndGet()
                videoTimestamp = info.presentationTimeUs
            }
            MediaCodec.MIME_AUDIO_MP4A -> {
                timestamp = (info.presentationTimeUs - audioTimestamp).toInt()
                val audio = stream.connection.messageFactory.createRtmpAudioMessage() as RtmpAacAudioMessage
                audio.aacPacketType = FlvAacPacketType.RAW
                audio.config = audioConfig
                audio.data = buffer
                audio.chunkStreamID = RtmpChunk.AUDIO
                audio.timestamp = timestamp / 1000
                audio.streamID = stream.id
                message = audio
                audioTimestamp = info.presentationTimeUs
            }
        }
        if (message != null) {
            stream.connection.doOutput(RtmpChunk.ONE, message)
        }
    }

    override fun onCaptureOutput(type: Int, buffer: ByteBuffer) {
        stream.listener?.onCaptureOutput(stream, type, buffer)
    }

    fun clear() {
        audioConfig = null
        audioTimestamp = 0L
        videoConfig = null
        videoTimestamp = 0L
    }

    companion object {
        private var TAG = RtmpMuxer::class.java.simpleName
    }
}
