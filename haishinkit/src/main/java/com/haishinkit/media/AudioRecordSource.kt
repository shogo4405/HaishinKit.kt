package com.haishinkit.media

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import com.haishinkit.codec.MediaCodec
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean

class AudioRecordSource() : AudioSource {
    internal class Callback(private val src: AudioRecordSource) : MediaCodec.Callback(MediaCodec.MIME.AUDIO_MP4A) {
        override fun onInputBufferAvailable(codec: android.media.MediaCodec, index: Int) {
            val result = src.read()
            val inputBuffer = codec.getInputBuffer(index)
            inputBuffer.clear()
            inputBuffer.put(src.buffer)
            codec.queueInputBuffer(index, 0, result, src.currentPresentationTimestamp, 0)
        }
    }
    var channel = DEFAULT_CHANNEL
    var audioSource = DEFAULT_AUDIO_SOURCE
    override var stream: RTMPStream? = null
    override val isRunning = AtomicBoolean(false)

    private var _buffer: ByteArray? = null
    var buffer: ByteArray
        get() {
            if (_buffer == null) {
                _buffer = ByteArray(minBufferSize)
            }
            return _buffer as ByteArray
        }
        set(value) {
            _buffer = value
        }

    var sampleRate = DEFAULT_SAMPLE_RATE

    private var _minBufferSize: Int = -1
    var minBufferSize: Int
        get() {
            if (_minBufferSize == -1) {
                _minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
            }
            return _minBufferSize
        }
        set(value) {
            _minBufferSize = value
        }

    private var _audioRecord: AudioRecord? = null
    var audioRecord: AudioRecord
        get() {
            if (_audioRecord == null) {
                if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
                    _audioRecord = AudioRecord.Builder()
                        .setAudioSource(audioSource)
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(encoding)
                                .setSampleRate(sampleRate)
                                .setChannelIndexMask(channel)
                                .build()
                        )
                        .setBufferSizeInBytes(minBufferSize * 2)
                        .build()
                } else {
                    _audioRecord = AudioRecord(
                        audioSource,
                        sampleRate,
                        channel,
                        encoding,
                        minBufferSize
                    )
                }
            }
            return _audioRecord as AudioRecord
        }
        set(value) {
            _audioRecord = value
        }

    var currentPresentationTimestamp: Long = 0L
        private set

    private var encoding = DEFAULT_ENCODING

    override fun setUp() {
        stream?.audioCodec?.callback = Callback(this)
    }

    override fun tearDown() {
    }

    override fun startRunning() {
        currentPresentationTimestamp = 0
        audioRecord.startRecording()
    }

    override fun stopRunning() {
        audioRecord.stop()
    }

    fun read(): Int {
        val result = audioRecord.read(buffer, 0, minBufferSize)
        currentPresentationTimestamp += timestamp()
        return result
    }

    private fun timestamp(): Long {
        return (1000000 * (minBufferSize / 2 / sampleRate)).toLong()
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT
        const val DEFAULT_SAMPLE_RATE = 44100
        const val DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.CAMCORDER
    }
}
