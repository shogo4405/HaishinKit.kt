package com.haishinkit.rtmp

import android.media.MediaFormat
import com.haishinkit.BuildConfig
import com.haishinkit.codec.MediaCodec
import com.haishinkit.flv.FlvAacPacketType
import com.haishinkit.flv.FlvAvcPacketType
import com.haishinkit.flv.FlvFlameType
import com.haishinkit.flv.FlvVideoCodec
import com.haishinkit.iso.AudioSpecificConfig
import com.haishinkit.iso.AvcConfigurationRecord
import com.haishinkit.metric.FrameTracker
import com.haishinkit.rtmp.messages.RtmpAacAudioMessage
import com.haishinkit.rtmp.messages.RtmpAvcVideoMessage
import com.haishinkit.rtmp.messages.RtmpMessage
import java.nio.ByteBuffer

internal class RtmpMuxer(private val stream: RtmpStream) : MediaCodec.Listener {
    private var audioTimestamp = 0L
    private var audioConfig: AudioSpecificConfig? = null
    private var videoTimestamp = 0L
    private var videoConfig: AvcConfigurationRecord? = null
    private var frameTracker: FrameTracker? = null
        get() {
            if (field == null && BuildConfig.DEBUG) {
                field = FrameTracker()
            }
            return field
        }

    override fun onFormatChanged(mime: String, mediaFormat: MediaFormat) {
        var message: RtmpMessage? = null
        when (mime) {
            MediaCodec.MIME_VIDEO_AVC -> {
                videoConfig = AvcConfigurationRecord.create(mediaFormat)
                val video = stream.messageFactory.createRtmpVideoMessage() as RtmpAvcVideoMessage
                video.packetType = FlvAvcPacketType.SEQ
                video.frame = FlvFlameType.KEY
                video.codec = FlvVideoCodec.AVC
                video.data = videoConfig?.toByteBuffer()
                video.chunkStreamID = RtmpChunk.VIDEO
                video.streamID = stream.id
                video.timestamp = 0
                message = video
            }
            MediaCodec.MIME_AUDIO_MP4A -> {
                val buffer = mediaFormat.getByteBuffer("csd-0") ?: return
                audioConfig = AudioSpecificConfig.create(buffer)
                val audio = stream.messageFactory.createRtmpAudioMessage() as RtmpAacAudioMessage
                audio.config = audioConfig
                audio.aacPacketType = FlvAacPacketType.SEQ
                audio.data = buffer
                audio.chunkStreamID = RtmpChunk.AUDIO
                audio.streamID = stream.id
                audio.timestamp = 0
                message = audio
            }
        }
        if (message != null) {
            stream.doOutput(RtmpChunk.ZERO, message)
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
                frameTracker?.track(FrameTracker.TYPE_VIDEO, System.currentTimeMillis())
                if (videoTimestamp == 0L) {
                    videoTimestamp = info.presentationTimeUs
                }
                timestamp = (info.presentationTimeUs - videoTimestamp).toInt()
                val keyframe = info.flags and android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME != 0
                val video = stream.messageFactory.createRtmpVideoMessage() as RtmpAvcVideoMessage
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
                frameTracker?.track(FrameTracker.TYPE_AUDIO, System.currentTimeMillis())
                if (audioTimestamp == 0L) {
                    audioTimestamp = info.presentationTimeUs
                }
                timestamp = (info.presentationTimeUs - audioTimestamp).toInt()
                val audio = stream.messageFactory.createRtmpAudioMessage() as RtmpAacAudioMessage
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
            stream.doOutput(RtmpChunk.ONE, message)
        }
    }

    override fun onCaptureOutput(type: Byte, buffer: ByteBuffer, timestamp: Long) {
        stream.listener?.onCaptureOutput(stream, type, buffer, timestamp)
    }

    fun clear() {
        audioConfig = null
        audioTimestamp = 0L
        videoConfig = null
        videoTimestamp = 0L
        frameTracker?.clear()
    }

    companion object {
        private var TAG = RtmpMuxer::class.java.simpleName
    }
}
