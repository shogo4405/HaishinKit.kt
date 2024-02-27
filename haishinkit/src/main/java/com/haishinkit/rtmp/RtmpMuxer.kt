package com.haishinkit.rtmp

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.SystemClock
import android.util.Log
import com.haishinkit.BuildConfig
import com.haishinkit.codec.Codec
import com.haishinkit.event.Event
import com.haishinkit.flv.FlvAacPacketType
import com.haishinkit.flv.FlvAudioCodec
import com.haishinkit.flv.FlvAvcPacketType
import com.haishinkit.flv.FlvFlameType
import com.haishinkit.flv.FlvVideoCodec
import com.haishinkit.iso.AvcConfigurationRecord
import com.haishinkit.lang.Running
import com.haishinkit.media.BufferController
import com.haishinkit.media.MediaLink
import com.haishinkit.metrics.FrameTracker
import com.haishinkit.rtmp.message.RtmpAudioMessage
import com.haishinkit.rtmp.message.RtmpVideoMessage
import com.haishinkit.util.MediaFormatUtil
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal class RtmpMuxer(private val stream: RtmpStream) :
    Running,
    BufferController.Listener,
    Codec.Listener {
    override var isRunning = AtomicBoolean(false)

    var mode = Codec.Mode.DECODE
        set(value) {
            stream.audioCodec.mode = value
            stream.videoCodec.mode = value
            field = value
        }

    var hasAudio: Boolean
        get() = mediaLink.hasAudio
        set(value) {
            mediaLink.hasAudio = value
        }

    var hasVideo: Boolean
        get() = mediaLink.hasVideo
        set(value) {
            mediaLink.hasVideo = value
        }

    private var bufferTime = BufferController.DEFAULT_BUFFER_TIME
        set(value) {
            audioBufferController.bufferTime = bufferTime
            videoBufferController.bufferTime = bufferTime
            field = value
        }

    private var audioTimestamp = 0L
    private var videoTimestamp = 0L
    private var frameTracker: FrameTracker? = null
        get() {
            if (field == null && BuildConfig.DEBUG) {
                field = FrameTracker()
            }
            return field
        }
    private val keyframes = mutableMapOf<Int, Boolean>()
    private val mediaLink: MediaLink by lazy {
        MediaLink(stream.audioCodec, stream.videoCodec)
    }
    private val audioBufferController: BufferController<RtmpAudioMessage> by lazy {
        val controller = BufferController<RtmpAudioMessage>("audio")
        controller.bufferTime = bufferTime
        controller.listener = this
        controller
    }

    private val videoBufferController: BufferController<RtmpVideoMessage> by lazy {
        val controller = BufferController<RtmpVideoMessage>("video")
        controller.bufferTime = bufferTime
        controller.listener = this
        controller
    }

    fun enqueueAudio(message: RtmpAudioMessage) {
        audioBufferController.enqueue(message, message.timestamp)
    }

    fun enqueueVideo(message: RtmpVideoMessage) {
        videoBufferController.enqueue(message, message.timestamp)
    }

    @Synchronized
    override fun startRunning() {
        if (isRunning.get()) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "startRunning()")
        }
        when (mode) {
            Codec.Mode.ENCODE -> {
                stream.audioSource?.let {
                    it.registerAudioCodec(stream.audioCodec)
                    stream.audioCodec.listener = this
                    stream.audioCodec.startRunning()
                }
                stream.videoSource?.let {
                    stream.videoCodec.listener = this
                    stream.videoCodec.startRunning()
                }
            }

            Codec.Mode.DECODE -> {
                mediaLink.startRunning()
            }
        }
        isRunning.set(true)
    }

    @Synchronized
    override fun stopRunning() {
        if (!isRunning.get()) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "stopRunning()")
        }
        when (mode) {
            Codec.Mode.ENCODE -> {
                stream.audioCodec.stopRunning()
                stream.videoCodec.stopRunning()
                stream.audioSource?.unregisterAudioCodec(stream.audioCodec)
            }

            Codec.Mode.DECODE -> {
                clear()
                mediaLink.stopRunning()
            }
        }
        isRunning.set(false)
    }

    fun clear() {
        audioTimestamp = 0L
        videoTimestamp = 0L
        frameTracker?.clear()
        audioBufferController.clear()
        videoBufferController.clear()
        mediaLink.clear()
    }

    override fun onInputBufferAvailable(
        mime: String,
        codec: MediaCodec,
        index: Int,
    ) {
        when (mime) {
            Codec.MIME_VIDEO_RAW -> {
                try {
                    val inputBuffer = codec.getInputBuffer(index) ?: return
                    val message = videoBufferController.take()
                    videoBufferController.consume(message.timestamp)
                    val success =
                        message.data?.let {
                            it.position(4)
                            if (it.remaining() <= inputBuffer.remaining()) {
                                inputBuffer.put(it)
                                true
                            } else {
                                Log.w(TAG, "BufferOverrun will drop a RTMPVideoMessage")
                                false
                            }
                        } ?: false
                    videoTimestamp += message.timestamp * 1000
                    if (success) {
                        // There are some devices where info.flags always become 0, so this is a workaround.
                        keyframes[index] = message.frame == FlvFlameType.KEY
                        codec.queueInputBuffer(
                            index,
                            0,
                            message.length - 5,
                            videoTimestamp,
                            message.toFlags(),
                        )
                    }
                    message.release()
                } catch (e: InterruptedException) {
                    Log.w(TAG, "", e)
                }
            }

            Codec.MIME_AUDIO_RAW -> {
                try {
                    val inputBuffer = codec.getInputBuffer(index) ?: return
                    audioBufferController.stop()
                    val message = audioBufferController.take()
                    audioBufferController.consume(message.timestamp)
                    message.data?.let {
                        it.position(1)
                        inputBuffer.put(it)
                    }
                    audioTimestamp += message.timestamp * 1000
                    codec.queueInputBuffer(
                        index,
                        0,
                        message.length - 2,
                        audioTimestamp,
                        message.toFlags(),
                    )
                    message.release()
                } catch (e: InterruptedException) {
                    Log.w(TAG, "", e)
                }
            }

            else -> {
            }
        }
    }

    override fun onFormatChanged(
        mime: String,
        mediaFormat: MediaFormat,
    ) {
        when (mime) {
            Codec.MIME_VIDEO_RAW -> {
                stream.dispatchEventWith(
                    Event.RTMP_STATUS,
                    false,
                    RtmpStream.Code.VIDEO_DIMENSION_CHANGE.data(""),
                )
            }

            Codec.MIME_VIDEO_AVC -> {
                val config = AvcConfigurationRecord.create(mediaFormat)
                val video = stream.messageFactory.createRtmpVideoMessage()
                video.packetType = FlvAvcPacketType.SEQ
                video.frame = FlvFlameType.KEY
                video.codec = FlvVideoCodec.AVC
                video.data =
                    config.allocate().apply {
                        config.encode(this)
                        flip()
                    }
                video.chunkStreamID = RtmpChunk.VIDEO
                video.streamID = stream.id
                video.timestamp = 0
                video.compositeTime = 0
                stream.doOutput(RtmpChunk.ZERO, video)
            }

            Codec.MIME_AUDIO_RAW -> {
                mediaLink.audioTrack = MediaFormatUtil.createAudioTrack(mediaFormat)
            }

            Codec.MIME_AUDIO_MP4A -> {
                val config = mediaFormat.getByteBuffer("csd-0") ?: return
                val audio = stream.messageFactory.createRtmpAudioMessage()
                audio.codec = FlvAudioCodec.AAC
                audio.aacPacketType = FlvAacPacketType.SEQ
                audio.data = config
                audio.chunkStreamID = RtmpChunk.AUDIO
                audio.streamID = stream.id
                audio.timestamp = 0
                stream.doOutput(RtmpChunk.ZERO, audio)
            }
        }
    }

    override fun onSampleOutput(
        mime: String,
        index: Int,
        info: MediaCodec.BufferInfo,
        buffer: ByteBuffer,
    ): Boolean {
        when (mime) {
            Codec.MIME_VIDEO_RAW -> {
                mediaLink.queueVideo(
                    index,
                    null,
                    info.presentationTimeUs,
                    keyframes[index] ?: false,
                )
                return false
            }

            Codec.MIME_VIDEO_AVC -> {
                if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    return true
                }
                frameTracker?.track(FrameTracker.TYPE_VIDEO, SystemClock.uptimeMillis())
                if (videoTimestamp == 0L) {
                    val diff =
                        if (stream.audioSource != null && 0 < audioTimestamp) {
                            info.presentationTimeUs - audioTimestamp / 1000
                        } else {
                            0
                        }
                    videoTimestamp = info.presentationTimeUs - diff
                }
                val timestamp = (info.presentationTimeUs - videoTimestamp).toInt()
                val keyframe = info.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0
                val video = stream.messageFactory.createRtmpVideoMessage()
                video.packetType = FlvAvcPacketType.NAL
                video.frame = if (keyframe) FlvFlameType.KEY else FlvFlameType.INTER
                video.codec = FlvVideoCodec.AVC
                video.data = buffer
                video.chunkStreamID = RtmpChunk.VIDEO
                video.timestamp = timestamp / 1000
                video.streamID = stream.id
                video.compositeTime = 0
                stream.doOutput(RtmpChunk.ONE, video)
                stream.frameCount.incrementAndGet()
                videoTimestamp += video.timestamp * 1000
                return true
            }

            Codec.MIME_AUDIO_RAW -> {
                mediaLink.queueAudio(index, buffer, info.presentationTimeUs, true)
                return false
            }

            Codec.MIME_AUDIO_MP4A -> {
                if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    return true
                }
                frameTracker?.track(FrameTracker.TYPE_AUDIO, SystemClock.uptimeMillis())
                if (audioTimestamp == 0L) {
                    audioTimestamp = info.presentationTimeUs
                }
                val timestamp = (info.presentationTimeUs - audioTimestamp).toInt()
                val audio = stream.messageFactory.createRtmpAudioMessage()
                audio.codec = FlvAudioCodec.AAC
                audio.aacPacketType = FlvAacPacketType.RAW
                audio.data = buffer
                audio.chunkStreamID = RtmpChunk.AUDIO
                audio.timestamp = timestamp / 1000
                audio.streamID = stream.id
                stream.doOutput(RtmpChunk.ONE, audio)
                audioTimestamp += audio.timestamp * 1000
                return true
            }
        }
        return true
    }

    override fun <T> onBufferFull(controller: BufferController<T>) {
        if (controller == videoBufferController) {
            if (stream.receiveVideo) {
                mediaLink.paused = false
            } else {
                return
            }
        }
        stream.dispatchEventWith(Event.RTMP_STATUS, false, RtmpStream.Code.BUFFER_FULL.data(""))
    }

    override fun <T> onBufferEmpty(controller: BufferController<T>) {
        if (controller == videoBufferController) {
            if (stream.receiveVideo) {
                mediaLink.paused = true
            } else {
                return
            }
        }
        stream.dispatchEventWith(Event.RTMP_STATUS, false, RtmpStream.Code.BUFFER_EMPTY.data(""))
    }

    companion object {
        private const val VERBOSE = false
        private var TAG = RtmpMuxer::class.java.simpleName
    }
}
