package com.haishinkit.rtmp

import android.media.MediaCodec
import android.media.MediaFormat
import com.haishinkit.codec.Codec
import com.haishinkit.flv.AACPacketType
import com.haishinkit.flv.AVCPacketType
import com.haishinkit.flv.FlameType
import com.haishinkit.flv.VideoCodec
import com.haishinkit.iso.AVCConfigurationRecord
import com.haishinkit.iso.AVCFormatUtils
import com.haishinkit.iso.AudioSpecificConfig
import com.haishinkit.rtmp.messages.RTMPAACAudioMessage
import com.haishinkit.rtmp.messages.RTMPAVCVideoMessage
import com.haishinkit.rtmp.messages.RTMPMessage
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

internal class RTMPMuxer(private val stream: RTMPStream) : Codec.Listener {
    private val timestamps = ConcurrentHashMap<String, Long>()
    private var audioConfig: AudioSpecificConfig? = null
    private var videoConfig: AVCConfigurationRecord? = null

    override fun onFormatChanged(mime: Codec.MIME, mediaFormat: MediaFormat) {
        var message: RTMPMessage? = null
        when (mime) {
            Codec.MIME.VIDEO_AVC -> {
                videoConfig = AVCConfigurationRecord(mediaFormat)
                var video = stream.connection.messageFactory.createRTMPVideoMessage() as RTMPAVCVideoMessage
                video.packetType = AVCPacketType.SEQ.rawValue
                video.frame = FlameType.KEY.rawValue
                video.codec = VideoCodec.AVC.rawValue
                video.payload = videoConfig!!.toByteBuffer()
                video.chunkStreamID = RTMPChunk.VIDEO
                video.streamID = stream.id
                message = video
            }
            Codec.MIME.AUDIO_MP4A -> {
                val buffer = mediaFormat.getByteBuffer("csd-0")
                audioConfig = AudioSpecificConfig(buffer)
                var audio = stream.connection.messageFactory.createRTMPAudioMessage() as RTMPAACAudioMessage
                audio.config = audioConfig
                audio.aacPacketType = AACPacketType.SEQ.rawValue
                audio.payload = buffer
                audio.chunkStreamID = RTMPChunk.AUDIO
                audio.streamID = stream.id
                message = audio
            }
        }
        if (message != null) {
            stream.connection.doOutput(RTMPChunk.ZERO, message)
        }
    }

    override fun onSampleOutput(mime: Codec.MIME, info: MediaCodec.BufferInfo, buffer: ByteBuffer) {
        if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
            return
        }
        var timestamp = 0
        var message: RTMPMessage? = null
        if (timestamps.containsKey(mime.rawValue)) {
            timestamp = (info.presentationTimeUs - timestamps[mime.rawValue]!!.toLong()).toInt()
        }
        when (mime) {
            Codec.MIME.VIDEO_AVC -> {
                val keyframe = info.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0
                var video = stream.connection.messageFactory.createRTMPVideoMessage() as RTMPAVCVideoMessage
                video.packetType = AVCPacketType.NAL.rawValue
                video.frame = if (keyframe) FlameType.KEY.rawValue else FlameType.INTER.rawValue
                video.codec = VideoCodec.AVC.rawValue
                video.payload = AVCFormatUtils.toNALFileFormat(buffer)
                video.chunkStreamID = RTMPChunk.VIDEO
                video.timestamp = timestamp
                video.streamID = stream.id
                message = video
                stream.frameCount.incrementAndGet()
            }
            Codec.MIME.AUDIO_MP4A -> {
                var audio = stream.connection.messageFactory.createRTMPAudioMessage() as RTMPAACAudioMessage
                audio.aacPacketType = AACPacketType.RAW.rawValue
                audio.config = audioConfig
                audio.payload = buffer
                audio.chunkStreamID = RTMPChunk.AUDIO
                audio.timestamp = timestamp / 1000
                audio.streamID = stream.id
                message = audio
            }
        }
        if (message != null) {
            stream.connection.doOutput(RTMPChunk.ONE, message)
        }
        timestamps[mime.rawValue] = info.presentationTimeUs
    }

    fun clear() {
        timestamps.clear()
    }
}
