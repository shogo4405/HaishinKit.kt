package com.haishinkit.codec

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.haishinkit.lang.Running
import org.apache.commons.lang3.builder.ToStringBuilder
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal abstract class MediaCodec(private val mime: MIME) : Running {
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
        fun onFormatChanged(mime: String, mediaFormat: MediaFormat)
        fun onSampleOutput(mime: String, info: MediaCodec.BufferInfo, buffer: ByteBuffer)
    }

    internal open class Callback : android.media.MediaCodec.Callback() {
        var listener: Listener? = null
        var codec: com.haishinkit.codec.MediaCodec? = null

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        }

        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: android.media.MediaCodec.BufferInfo) {
            try {
                val buffer = codec.getOutputBuffer(index) ?: return
                listener?.onSampleOutput(codec.outputFormat.getString("mime"), info, buffer)
                codec.releaseOutputBuffer(index, false)
            } catch (e: IllegalStateException) {
                Log.w(javaClass.name, e)
            }
        }

        override fun onError(codec: MediaCodec, e: android.media.MediaCodec.CodecException) {
            Log.w(javaClass.name, e.toString())
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            this.codec?.outputFormat = format
        }
    }

    var listener: Listener? = null
        set(value) {
            field = value
            callback?.listener = value
        }
    var callback: Callback? = null
        set(value) {
            field = value
            callback?.codec = this
        }
    var _codec: MediaCodec? = null
    var codec: MediaCodec?
        get() {
            if (_codec == null) {
                _codec = MediaCodec.createEncoderByType(mime.rawValue).apply {
                    if (callback != null) {
                        callback?.listener = listener
                        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
                            this.setCallback(callback, backgroundHandler)
                        } else {
                            this.setCallback(callback)
                        }
                    }
                    this.configure(createOutputFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
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

    private var outputFormat: MediaFormat? = null
        set(value) {
            if (field != value && value != null) {
                Log.i(javaClass.name, value.toString())
                listener?.onFormatChanged(value.getString("mime"), value)
            }
            field = value
        }
    private val backgroundHandler by lazy {
        var thread = HandlerThread(javaClass.name)
        thread.start()
        Handler(thread.looper)
    }

    @Synchronized final override fun startRunning() {
        if (isRunning.get()) {
            return
        }
        try {
            val codec = codec ?: return
            outputFormat?.let {
                listener?.onFormatChanged(it.getString("mime"), it)
            }
            codec.start()
            isRunning.set(true)
        } catch (e: MediaCodec.CodecException) {
            Log.w(javaClass.name, ToStringBuilder.reflectionToString(outputFormat), e)
        }
    }

    @Synchronized final override fun stopRunning() {
        if (!isRunning.get()) {
            return
        }
        codec?.flush()
        isRunning.set(false)
    }

    protected abstract fun createOutputFormat(): MediaFormat

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
