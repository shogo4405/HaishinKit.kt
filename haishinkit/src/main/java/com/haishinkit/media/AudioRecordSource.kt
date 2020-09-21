package com.haishinkit.media

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaRecorder
import com.haishinkit.codec.Codec
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder

class AudioRecordSource() : AudioSource, AudioRecord.OnRecordPositionUpdateListener {
    internal class Callback(private val src: AudioRecordSource) : Codec.Callback(Codec.MIME.AUDIO_MP4A) {
        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            val result = src.read()
            val inputBuffer = codec.getInputBuffer(index)
            inputBuffer.clear()
            inputBuffer.put(src.buffer)
            codec.queueInputBuffer(index, 0, result, src.currentPresentationTimestamp, 0)
        }
    }
    var channel = DEFAULT_CHANNEL
    override var stream: RTMPStream? = null
    override var isRunning: Boolean = false
    private var encoding = DEFAULT_ENCODING

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

    var samplingRate = DEFAULT_SAMPLING_RATE

    private var _minBufferSize: Int = -1
    var minBufferSize: Int
        get() {
            if (_minBufferSize == -1) {
                _minBufferSize = AudioRecord.getMinBufferSize(samplingRate, channel, encoding)
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
                _audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    samplingRate,
                    channel,
                    encoding,
                    minBufferSize
                )
            }
            return _audioRecord as AudioRecord
        }
        set(value) {
            _audioRecord = value
        }

    var currentPresentationTimestamp: Long = 0L
        private set

    override fun setUp() {
        stream?.audioCodec?.callback = Callback(this)
        audioRecord?.positionNotificationPeriod = minBufferSize / 2
        // audioRecord?.setRecordPositionUpdateListener(this)
    }

    override fun tearDown() {
        // audioRecord?.setRecordPositionUpdateListener(null)
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

    override fun onMarkerReached(audio: AudioRecord?) {
    }

    override fun onPeriodicNotification(audio: AudioRecord?) {
        // audio?.read(buffer, 0, minBufferSize)
        // stream?.appendBytes(buffer, BufferInfo(BufferType.AUDIO, currentPresentationTimestamp.toLong()))
        // currentPresentationTimestamp += timestamp()
    }

    private fun timestamp(): Long {
        return (1000000 * (minBufferSize / 2 / samplingRate)).toLong()
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT
        const val DEFAULT_SAMPLING_RATE = 44100
    }
}
