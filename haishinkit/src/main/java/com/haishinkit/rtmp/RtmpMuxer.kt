package com.haishinkit.rtmp

import android.media.MediaFormat
import com.haishinkit.codec.MediaCodec
import com.haishinkit.flv.AacPacketType
import com.haishinkit.flv.AvcPacketType
import com.haishinkit.flv.FlameType
import com.haishinkit.flv.VideoCodec
import com.haishinkit.iso.AvcConfigurationRecord
import com.haishinkit.iso.AvcFormatUtils
import com.haishinkit.iso.AudioSpecificConfig
import com.haishinkit.rtmp.messages.RtmpAacAudioMessage
import com.haishinkit.rtmp.messages.RtmpAvcVideoMessage
import com.haishinkit.rtmp.messages.RtmpMessage
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

internal class RtmpMuxer(private val stream: RtmpStream) : MediaCodec.Listener {
    private val timestamps = ConcurrentHashMap<String, Long>()
    private var audioConfig: AudioSpecificConfig? = null
    private var videoConfig: AvcConfigurationRecord? = null

    override fun onFormatChanged(mime: String, mediaFormat: MediaFormat) {
        var message: RtmpMessage? = null
        when (mime) {
            MediaCodec.MIME_VIDEO_AVC -> {
                videoConfig = AvcConfigurationRecord.create(mediaFormat)
                val video = stream.connection.messageFactory.createRTMPVideoMessage() as RtmpAvcVideoMessage
                video.packetType = AvcPacketType.SEQ.toByte()
                video.frame = FlameType.KEY
                video.codec = VideoCodec.AVC
                video.payload = videoConfig!!.toByteBuffer()
                video.chunkStreamID = RtmpChunk.VIDEO
                video.streamID = stream.id
                message = video
            }
            MediaCodec.MIME_AUDIO_MP4A -> {
                val buffer = mediaFormat.getByteBuffer("csd-0") ?: return
                audioConfig = AudioSpecificConfig.create(buffer)
                val audio = stream.connection.messageFactory.createRTMPAudioMessage() as RtmpAacAudioMessage
                audio.config = audioConfig
                audio.aacPacketType = AacPacketType.SEQ.toByte()
                audio.payload = buffer
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
        var timestamp = 0
        var message: RtmpMessage? = null
        if (timestamps.containsKey(mime)) {
            timestamp = (info.presentationTimeUs - timestamps[mime]!!.toLong()).toInt()
        }
        when (mime) {
            MediaCodec.MIME_VIDEO_AVC -> {
                val keyframe = info.flags and android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME != 0
                val video = stream.connection.messageFactory.createRTMPVideoMessage() as RtmpAvcVideoMessage
                video.packetType = AvcPacketType.NAL
                video.frame = if (keyframe) FlameType.KEY else FlameType.INTER
                video.codec = VideoCodec.AVC
                video.payload = AvcFormatUtils.toNALFileFormat(buffer)
                video.chunkStreamID = RtmpChunk.VIDEO
                video.timestamp = timestamp / 1000
                video.streamID = stream.id
                message = video
                stream.frameCount.incrementAndGet()
            }
            MediaCodec.MIME_AUDIO_MP4A -> {
                val audio = stream.connection.messageFactory.createRTMPAudioMessage() as RtmpAacAudioMessage
                audio.aacPacketType = AacPacketType.RAW
                audio.config = audioConfig
                audio.payload = buffer
                audio.chunkStreamID = RtmpChunk.AUDIO
                audio.timestamp = timestamp / 1000
                audio.streamID = stream.id
                message = audio
            }
        }
        if (message != null) {
            stream.connection.doOutput(RtmpChunk.ONE, message)
        }
        timestamps[mime] = info.presentationTimeUs
    }

    fun clear() {
        timestamps.clear()
    }
}
