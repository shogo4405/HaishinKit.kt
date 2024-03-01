package com.haishinkit.media

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import com.haishinkit.codec.Codec
import com.haishinkit.lang.Running
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal class MediaMuxer(stream: Stream?, private var muxer: MediaMuxer?) :
    Running,
    Codec.Listener {
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    private var stream = WeakReference(stream)
    private val isReady: Boolean
        get() =
            (stream.get()?.audioSource != null && DEFAULT_TRACK_INDEX < audioTrackIndex) &&
                (stream.get()?.videoSource != null && DEFAULT_TRACK_INDEX < videoTrackIndex)
    private var audioTrackIndex = DEFAULT_TRACK_INDEX
    private var videoTrackIndex = DEFAULT_TRACK_INDEX

    override fun startRunning() {
        if (isRunning.get()) return
        if (!isReady) return
        muxer?.start()
        isRunning.set(true)
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        isRunning.set(false)
        muxer?.stop()
        muxer?.release()
        stream.clear()
    }

    override fun onInputBufferAvailable(
        mime: String,
        codec: MediaCodec,
        index: Int,
    ) {
    }

    override fun onFormatChanged(
        mime: String,
        mediaFormat: MediaFormat,
    ) {
        if (mime.contains("audio")) {
            audioTrackIndex = muxer?.addTrack(mediaFormat) ?: DEFAULT_TRACK_INDEX
            startRunning()
        }
        if (mime.contains("video")) {
            videoTrackIndex = muxer?.addTrack(mediaFormat) ?: DEFAULT_TRACK_INDEX
            startRunning()
        }
    }

    override fun onSampleOutput(
        mime: String,
        index: Int,
        info: MediaCodec.BufferInfo,
        buffer: ByteBuffer,
    ): Boolean {
        if (!isRunning.get()) return true
        var trackIndex = -1
        if (mime.contains("audio")) {
            trackIndex = audioTrackIndex
        }
        if (mime.contains("video")) {
            trackIndex = videoTrackIndex
        }
        muxer?.writeSampleData(trackIndex, buffer, info)
        return true
    }

    private companion object {
        private const val DEFAULT_TRACK_INDEX = -1
        private val TAG = MediaMuxer::class.java.simpleName
    }
}
