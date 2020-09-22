package com.haishinkit.codec

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import com.haishinkit.lang.Running
import com.haishinkit.yuv.NullByteConverter
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal abstract class Codec(private val mime: MIME) : Running {
    internal enum class MIME(val rawValue: String) {
        VIDEO_VP8("video/x-vnd.on2.vp8"),
        VIDEO_VP9("video/x-vnd.on2.vp9"),
        VIDEO_AVC("video/avc"),
        VIDEO_HEVC("video/hevc"),
        VIDEO_MP4V("video/mp4v-es"),
        VIDEO_3GPP("video/3gpp"),
        AUDIO_3GPP("audio/3gpp"),
        AUDIO_AMR("audio/amr-wb"),
        AUDIO_MPEG("audio/mpeg"),
        AUDIO_MP4A("audio/mp4a-latm"),
        AUDIO_VORBIS("audio/vorbis"),
        AUDIO_G711A("audio/g711-alaw"),
        AUDIO_G711U("audio/g711-mlaw")
    }

    internal interface Listener {
        fun onFormatChanged(mime: MIME, mediaFormat: MediaFormat)
        fun onSampleOutput(mime: MIME, info: MediaCodec.BufferInfo, buffer: ByteBuffer)
    }

    internal open class Callback(
        private val mime: MIME
    ) : MediaCodec.Callback() {
        var listener: Listener? = null

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        }

        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            val buffer = codec.getOutputBuffer(index)
            listener?.onSampleOutput(mime, info, buffer)
            codec.releaseOutputBuffer(index, false)
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            Log.w(javaClass.name, e.toString())
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            listener?.onFormatChanged(mime, format)
        }
    }

    var byteConverter: ByteConverter = NullByteConverter.instance
    var listener: Listener? = null
        set(value) {
            field = value
            callback?.listener = value
        }
    var callback: Callback? = null
    private var _codec: MediaCodec? = null
    protected var codec: MediaCodec?
        get() {
            if (_codec == null) {
                _codec = MediaCodec.createEncoderByType(mime.rawValue).apply {
                    this.configure(this@Codec.createOutputFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                    if (callback != null) {
                        callback?.listener = listener
                        this.setCallback(callback)
                    }
                }
            }
            return _codec
        }
        set(value) {
            _codec?.stop()
            _codec?.release()
            _codec = value
        }

    override val isRunning = AtomicBoolean(false)

    final override fun startRunning() {
        synchronized(this) {
            if (isRunning.get()) {
                return
            }
            try {
                codec?.start()
                isRunning.set(true)
            } catch (e: IOException) {
                Log.w(javaClass.name, e)
            }
        }
    }

    final override fun stopRunning() {
        synchronized(this) {
            if (!isRunning.get()) {
                return
            }
            codec = null
            isRunning.set(false)
        }
    }

    fun appendBytes(bytes: ByteArray, info: BufferInfo) {
    }

    protected abstract fun createOutputFormat(): MediaFormat
}
