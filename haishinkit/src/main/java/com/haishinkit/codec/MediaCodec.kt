package com.haishinkit.codec

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.haishinkit.BuildConfig
import com.haishinkit.lang.Running
import org.apache.commons.lang3.builder.ToStringBuilder
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal abstract class MediaCodec(private val mime: String) : Running {
    interface Listener {
        fun onFormatChanged(mime: String, mediaFormat: MediaFormat)
        fun onSampleOutput(mime: String, info: MediaCodec.BufferInfo, buffer: ByteBuffer)
    }

    open class Callback : android.media.MediaCodec.Callback() {
        var listener: Listener? = null
        var codec: com.haishinkit.codec.MediaCodec? = null

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        }

        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: android.media.MediaCodec.BufferInfo) {
            try {
                val buffer = codec.getOutputBuffer(index) ?: return
                codec.outputFormat.getString("mime")?.let { mime ->
                    listener?.onSampleOutput(mime, info, buffer)
                }
                codec.releaseOutputBuffer(index, false)
            } catch (e: IllegalStateException) {
                if (BuildConfig.DEBUG) { Log.w(javaClass.name, e) }
            }
        }

        override fun onError(codec: MediaCodec, e: android.media.MediaCodec.CodecException) {
            if (BuildConfig.DEBUG) { Log.w(javaClass.name, e.toString()) }
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
    private var _codec: MediaCodec? = null
    var codec: MediaCodec?
        get() {
            if (_codec == null) {
                _codec = MediaCodec.createEncoderByType(mime)
                _codec?.let { configure(it) }
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
                value.getString("mime")?.let { mime ->
                    listener?.onFormatChanged(mime, value)
                }
            }
            field = value
        }

    private var _backgroundHandler: Handler? = null
    private var backgroundHandler: Handler?
        get() {
            if (_backgroundHandler == null) {
                val thread = HandlerThread(javaClass.name)
                thread.start()
                _backgroundHandler = Handler(thread.looper)
            }
            return _backgroundHandler
        }
        set(value) {
            _backgroundHandler?.looper?.quitSafely()
            _backgroundHandler = value
        }

    @Synchronized final override fun startRunning() {
        if (isRunning.get()) {
            return
        }
        if (BuildConfig.DEBUG) {
            Log.d(javaClass.name, "startRunning()")
        }
        try {
            val codec = codec ?: return
            outputFormat?.let { format ->
                format.getString("mime")?.let { mime ->
                    listener?.onFormatChanged(mime, format)
                }
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
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "stopRunning()")
        }
        codec?.stop()
        codec?.let { configure(it) }
        outputFormat = null
        isRunning.set(false)
    }

    fun dispose() {
        codec = null
        outputFormat = null
    }

    open fun configure(codec: MediaCodec) {
        if (callback != null) {
            callback?.listener = listener
            if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
                codec.setCallback(callback, backgroundHandler)
            } else {
                codec.setCallback(callback)
            }
        }
        codec.configure(createOutputFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    protected abstract fun createOutputFormat(): MediaFormat

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val MIME_VIDEO_VP8 = "video/x-vnd.on2.vp8"
        const val MIME_VIDEO_VP9 = "video/x-vnd.on2.vp9"
        const val MIME_VIDEO_AVC = "video/avc"
        const val MIME_VIDEO_HEVC = "video/hevc"
        const val MIME_VIDEO_MP4V = "video/mp4v-es"
        const val MIME_VIDEO_3GPP = "video/3gpp"
        const val MIME_AUDIO_3GPP = "audio/3gpp"
        const val MIME_AUDIO_AMR = "audio/amr-wb"
        const val MIME_AUDIO_MPEG = "audio/mpeg"
        const val MIME_AUDIO_MP4A = "audio/mp4a-latm"
        const val MIME_AUDIO_VORBIS = "audio/vorbis"
        const val MIME_AUDIO_G711A = "audio/g711-alaw"
        const val MIME_AUDIO_G711U = "audio/g711-mlaw"

        private val TAG = MediaCodec::class.java.simpleName
    }
}
