package com.haishinkit.media

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import com.haishinkit.codec.Codec
import com.haishinkit.lang.Running
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal class MediaMuxer(stream: Stream?, private var muxer: MediaMuxer?) : Running,
    Codec.Listener {
    private var stream = WeakReference(stream)
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    private var audioTrackIndex: Int = -1
    private var videoTrackIndex: Int = -1

    override fun startRunning() {
        if (!isRunning.get()) return
        muxer?.start()
        isRunning.set(true)
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        isRunning.set(false)
        muxer?.stop()
        muxer?.release()
    }

    override fun onInputBufferAvailable(mime: String, codec: MediaCodec, index: Int) {
        if (mime.contains("audio")) {
            val inputBuffer = codec.getInputBuffer(index) ?: return
            val result = stream.get()?.audioSource?.read(inputBuffer) ?: 0
            codec.queueInputBuffer(
                index,
                0,
                result,
                0,
                0
            )
        }
    }

    override fun onFormatChanged(mime: String, mediaFormat: MediaFormat) {
        if (mime.contains("audio")) {
            audioTrackIndex = muxer?.addTrack(mediaFormat) ?: -1
            startRunning()
        }
        if (mime.contains("video")) {
            videoTrackIndex = muxer?.addTrack(mediaFormat) ?: -1
            startRunning()
        }
    }

    override fun onSampleOutput(
        mime: String, index: Int, info: MediaCodec.BufferInfo, buffer: ByteBuffer
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
}
