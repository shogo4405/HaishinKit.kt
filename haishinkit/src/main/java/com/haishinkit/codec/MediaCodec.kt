package com.haishinkit.codec

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import com.haishinkit.BuildConfig
import com.haishinkit.lang.Running
import com.haishinkit.media.MediaLink
import org.apache.commons.lang3.builder.ToStringBuilder
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.util.Deque
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

abstract class MediaCodec(private val mime: String) : Running {
    open class Setting(private var codec: com.haishinkit.codec.MediaCodec?) {
        var options: List<CodecOption> by Delegates.observable(listOf()) { _, _, newValue ->
            codec?.options = newValue
        }
        var capturing: Boolean by Delegates.observable(false) { _, _, newValue ->
            codec?.capturing = capturing
        }
    }

    interface Listener {
        fun onInputBufferAvailable(mime: String, codec: android.media.MediaCodec, index: Int)
        fun onFormatChanged(mime: String, mediaFormat: MediaFormat)
        fun onSampleOutput(mime: String, index: Int, info: MediaCodec.BufferInfo, buffer: ByteBuffer): Boolean
        fun onCaptureOutput(type: Byte, buffer: ByteBuffer, timestamp: Long)
    }

    class Callback : android.media.MediaCodec.Callback() {
        var listener: Listener? = null
        var codec: com.haishinkit.codec.MediaCodec? = null
        var mime: String = ""

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            try {
                listener?.onInputBufferAvailable(mime, codec, index)
            } catch (e: IllegalStateException) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, e)
                }
            }
        }

        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: android.media.MediaCodec.BufferInfo) {
            try {
                val buffer = codec.getOutputBuffer(index) ?: return
                if (listener?.onSampleOutput(mime, index, info, buffer) == true) {
                    codec.releaseOutputBuffer(index, info.presentationTimeUs * 1000)
                }
            } catch (e: IllegalStateException) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, e)
                }
            }
        }

        override fun onError(codec: MediaCodec, e: android.media.MediaCodec.CodecException) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, e.toString())
            }
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            this.codec?.outputFormat = format
        }
    }

    var listener: Listener? = null
        set(value) {
            field = value
            callback.listener = value
        }
    var codec: MediaCodec? = null
        get() {
            if (field == null) {
                field = if (mode == MODE_ENCODE) {
                    MediaCodec.createEncoderByType(mime)
                } else {
                    MediaCodec.createDecoderByType(mime)
                }
            }
            return field
        }
        set(value) {
            field?.stop()
            field?.release()
            field = value
        }
    var mode = MODE_ENCODE
    var options = listOf<CodecOption>()
    var inputSurface: Surface? = null
    @Volatile var capturing = false
    override val isRunning = AtomicBoolean(false)
    var outputFormat: MediaFormat? = null
        private set(value) {
            if (field != value && value != null) {
                Log.i(TAG, value.toString())
                field = value
                value.getString("mime")?.let { mime ->
                    listener?.onFormatChanged(mime, value)
                }
            }
            field = value
        }
    private var callback: Callback = Callback()
        set(value) {
            field = value
            callback.codec = this
        }
    private var backgroundHandler: Handler? = null
        get() {
            if (field == null) {
                val thread = HandlerThread(javaClass.name)
                thread.start()
                field = Handler(thread.looper)
            }
            return field
        }
        set(value) {
            field?.looper?.quitSafely()
            field = value
        }

    @Synchronized final override fun startRunning() {
        if (isRunning.get()) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "startRunning($mime)")
        }
        try {
            val codec = codec ?: return
            configure(codec)
            outputFormat?.let { format ->
                format.getString("mime")?.let { mime ->
                    listener?.onFormatChanged(mime, format)
                }
            }
            codec.start()
            isRunning.set(true)
        } catch (e: MediaCodec.CodecException) {
            Log.w(TAG, ToStringBuilder.reflectionToString(outputFormat), e)
        }
    }

    @Synchronized final override fun stopRunning() {
        if (!isRunning.get()) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "stopRunning($mime)")
        }
        try {
            codec = null
            backgroundHandler = null
            outputFormat = null
            isRunning.set(false)
        } catch (e: MediaCodec.CodecException) {
            Log.w(TAG, "", e)
        } catch (e: IllegalStateException) {
            Log.w(TAG, "", e)
        }
    }

    fun dispose() {
        codec = null
        outputFormat = null
    }

    open fun configure(codec: MediaCodec) {
        callback.codec = this
        callback.listener = listener
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            codec.setCallback(callback, backgroundHandler)
        } else {
            codec.setCallback(callback)
        }
        val format = createOutputFormat()
        for (option in options) {
            option.apply(format)
        }
        codec.configure(format, inputSurface, null, if (mode == MODE_ENCODE) { MediaCodec.CONFIGURE_FLAG_ENCODE } else { 0 })
        codec.outputFormat.getString("mime")?.let { mime ->
            callback.mime = mime
        }
    }

    internal fun release(buffers: Deque<MediaLink.Buffer>) {
        val it = buffers.iterator()
        while (it.hasNext()) {
            val buffer = it.next()
            codec?.releaseOutputBuffer(buffer.index, false)
            it.remove()
        }
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
        const val MIME_VIDEO_RAW = "video/raw"
        const val MIME_AUDIO_3GPP = "audio/3gpp"
        const val MIME_AUDIO_AMR = "audio/amr-wb"
        const val MIME_AUDIO_MPEG = "audio/mpeg"
        const val MIME_AUDIO_MP4A = "audio/mp4a-latm"
        const val MIME_AUDIO_VORBIS = "audio/vorbis"
        const val MIME_AUDIO_G711A = "audio/g711-alaw"
        const val MIME_AUDIO_G711U = "audio/g711-mlaw"
        const val MIME_AUDIO_RAW = "audio/raw"

        const val MODE_ENCODE = 0
        const val MODE_DECODE = 1

        private val TAG = MediaCodec::class.java.simpleName
    }
}
