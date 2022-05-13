package com.haishinkit.rtmp

import android.graphics.ImageFormat
import android.media.MediaFormat
import android.os.SystemClock
import android.util.Log
import com.haishinkit.BuildConfig
import com.haishinkit.codec.MediaCodec
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
import com.haishinkit.metric.FrameTracker
import com.haishinkit.rtmp.message.RtmpAudioMessage
import com.haishinkit.rtmp.message.RtmpVideoMessage
import com.haishinkit.util.MediaFormatUtil
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal class RtmpMuxer(private val stream: RtmpStream) :
    Running,
    BufferController.Listener,
    MediaCodec.Listener {
    override var isRunning = AtomicBoolean(false)

    var mode = MediaCodec.Mode.DECODE
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
    var bufferTime = BufferController.DEFAULT_BUFFER_TIME
        set(value) {
            audioBufferController.bufferTime = bufferTime
            videoBufferController.bufferTime = bufferTime
            field = value
        }

    private var hasFirstFlame = false
    private var audioTimestamp = 0L
    private var videoTimestamp = 0L
    private var frameTracker: FrameTracker? = null
        get() {
            if (field == null && BuildConfig.DEBUG) {
                field = FrameTracker()
            }
            return field
        }
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
        hasFirstFlame = false
        when (mode) {
            MediaCodec.Mode.ENCODE -> {
                stream.audio?.let {
                    it.startRunning()
                    stream.audioCodec.listener = this
                    stream.audioCodec.startRunning()
                }
                stream.video?.let {
                    it.startRunning()
                    stream.videoCodec.listener = this
                    stream.videoCodec.startRunning()
                }
            }
            MediaCodec.Mode.DECODE -> {
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
            MediaCodec.Mode.ENCODE -> {
                stream.audio?.stopRunning()
                stream.audioCodec.stopRunning()
                if (stream.renderer == null) {
                    stream.video?.stopRunning()
                }
                stream.videoCodec.stopRunning()
            }
            MediaCodec.Mode.DECODE -> {
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
    }

    override fun onInputBufferAvailable(mime: String, codec: android.media.MediaCodec, index: Int) {
        when (mime) {
            MediaCodec.MIME_AUDIO_MP4A -> {
                stream.audio?.onInputBufferAvailable(codec, index)
            }
            MediaCodec.MIME_VIDEO_RAW -> {
                try {
                    val inputBuffer = codec.getInputBuffer(index) ?: return
                    videoBufferController.stop(!hasFirstFlame)
                    val message = videoBufferController.take()
                    videoBufferController.consume(message.timestamp)
                    message.data?.let {
                        it.position(4)
                        inputBuffer.put(it)
                    }
                    videoTimestamp += message.timestamp * 1000
                    codec.queueInputBuffer(
                        index,
                        0,
                        message.length - 5,
                        videoTimestamp,
                        message.toFlags()
                    )
                    message.release()
                } catch (e: InterruptedException) {
                    Log.w(TAG, "", e)
                }
            }
            MediaCodec.MIME_AUDIO_RAW -> {
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
                        message.toFlags()
                    )
                    message.release()
                } catch (e: InterruptedException) {
                    Log.w(TAG, "", e)
                }
            }
        }
    }

    override fun onFormatChanged(mime: String, mediaFormat: MediaFormat) {
        when (mime) {
            MediaCodec.MIME_VIDEO_RAW -> {
                mediaFormat.apply {
                    stream.renderer?.createInputSurface(
                        mediaFormat.getInteger(MediaFormat.KEY_WIDTH),
                        mediaFormat.getInteger(MediaFormat.KEY_HEIGHT),
                        ImageFormat.YUV_420_888
                    ) {
                        stream.videoCodec.surface = it
                    }
                }
                stream.dispatchEventWith(
                    Event.RTMP_STATUS,
                    false,
                    RtmpStream.Code.VIDEO_DIMENSION_CHANGE.data("")
                )
            }
            MediaCodec.MIME_VIDEO_AVC -> {
                val config = AvcConfigurationRecord.create(mediaFormat)
                val video = stream.messageFactory.createRtmpVideoMessage()
                video.packetType = FlvAvcPacketType.SEQ
                video.frame = FlvFlameType.KEY
                video.codec = FlvVideoCodec.AVC
                video.data = config.allocate().apply {
                    config.encode(this)
                    flip()
                }
                video.chunkStreamID = RtmpChunk.VIDEO
                video.streamID = stream.id
                video.timestamp = 0
                video.compositeTime = 0
                stream.doOutput(RtmpChunk.ZERO, video)
            }
            MediaCodec.MIME_AUDIO_RAW -> {
                mediaLink.audioTrack = stream.createAudioTrack(mediaFormat)
            }
            MediaCodec.MIME_AUDIO_MP4A -> {
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
        info: android.media.MediaCodec.BufferInfo,
        buffer: ByteBuffer
    ): Boolean {
        when (mime) {
            MediaCodec.MIME_VIDEO_RAW -> {
                if (!hasFirstFlame) {
                    hasFirstFlame =
                        (info.flags and android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0
                    stream.videoCodec.codec?.releaseOutputBuffer(index, hasFirstFlame)
                    return false
                }
                mediaLink.queueVideo(
                    MediaLink.Buffer(
                        index,
                        null,
                        info.presentationTimeUs,
                        (info.flags and android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0
                    )
                )
                return false
            }
            MediaCodec.MIME_VIDEO_AVC -> {
                if (info.flags and android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    return true
                }
                frameTracker?.track(FrameTracker.TYPE_VIDEO, SystemClock.uptimeMillis())
                if (videoTimestamp == 0L) {
                    videoTimestamp = info.presentationTimeUs
                }
                val timestamp = (info.presentationTimeUs - videoTimestamp).toInt()
                val keyframe = info.flags and android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME != 0
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
                videoTimestamp = info.presentationTimeUs
                return true
            }
            MediaCodec.MIME_AUDIO_RAW -> {
                mediaLink.queueAudio(MediaLink.Buffer(index, buffer, info.presentationTimeUs, true))
                return false
            }
            MediaCodec.MIME_AUDIO_MP4A -> {
                if (info.flags and android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
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
                audioTimestamp = info.presentationTimeUs
                return true
            }
        }
        return true
    }

    override fun onCaptureOutput(type: Byte, buffer: ByteBuffer, timestamp: Long) {
        stream.listener?.onCaptureOutput(stream, type, buffer, timestamp)
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
