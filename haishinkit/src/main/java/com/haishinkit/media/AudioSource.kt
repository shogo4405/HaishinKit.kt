package com.haishinkit.media

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder

class AudioSource() : IAudioSource, AudioRecord.OnRecordPositionUpdateListener {
    var channel = DEFAULT_CHANNEL
    override var stream: RTMPStream? = null
    override var isRunning: Boolean = false
    private var encoding = DEFAULT_ENCODING

    private var _buffer: ByteArray? = null
    var buffer: ByteArray?
        get() {
            if (_buffer == null) {
                _buffer = ByteArray(minBufferSize)
            }
            return _buffer
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
    var audioRecord: AudioRecord?
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
            return _audioRecord
        }
        set(value) {
            _audioRecord = value
        }

    private var currentPresentationTimestamp: Double = 0.0

    override fun setUp() {
        stream?.getEncoderByName("audio/mp4a-latm")
        audioRecord?.positionNotificationPeriod = minBufferSize / 2
        audioRecord?.setRecordPositionUpdateListener(this)
    }

    override fun tearDown() {
        audioRecord?.setRecordPositionUpdateListener(null)
    }

    override fun startRunning() {
        currentPresentationTimestamp = 0.0
        audioRecord?.startRecording()
        audioRecord?.read(buffer, 0, minBufferSize)
    }

    override fun stopRunning() {
        audioRecord?.stop()
    }

    override fun onMarkerReached(audio: AudioRecord?) {
    }

    override fun onPeriodicNotification(audio: AudioRecord?) {
        audio?.read(buffer, 0, minBufferSize)
        stream?.appendBytes(buffer, currentPresentationTimestamp.toLong(), RTMPStream.BufferType.AUDIO)
        currentPresentationTimestamp += timestamp()
    }

    private fun timestamp(): Double {
        return 1000000 * (minBufferSize.toDouble() / 2 / samplingRate.toDouble())
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
