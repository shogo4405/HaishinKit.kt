package com.haishinkit.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.haishinkit.BuildConfig
import com.haishinkit.net.NetStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * An audio source that captures a microphone by the AudioRecord api.
 */
class AudioRecordSource(
    private val context: Context,
    override var utilizable: Boolean = false
) : AudioSource {
    var channel = DEFAULT_CHANNEL
    var audioSource = DEFAULT_AUDIO_SOURCE
    var sampleRate = DEFAULT_SAMPLE_RATE

    override var stream: NetStream? = null
    override val isRunning = AtomicBoolean(false)

    var minBufferSize = -1
        get() {
            if (field == -1) {
                field = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
            }
            return field
        }

    var audioRecord: AudioRecord? = null
        get() {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
            if (field == null) {
                if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
                    field = AudioRecord.Builder()
                        .setAudioSource(audioSource)
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(encoding)
                                .setSampleRate(sampleRate)
                                .setChannelMask(channel)
                                .build()
                        )
                        .setBufferSizeInBytes(minBufferSize)
                        .build()
                } else {
                    field = AudioRecord(
                        audioSource,
                        sampleRate,
                        channel,
                        encoding,
                        minBufferSize
                    )
                }
            }
            return field
        }

    override var currentPresentationTimestamp = DEFAULT_TIMESTAMP
        private set

    private var encoding = DEFAULT_ENCODING
    private var sampleCount = DEFAULT_SAMPLE_COUNT

    override fun setUp() {
        if (utilizable) return
        currentPresentationTimestamp = DEFAULT_TIMESTAMP
        audioRecord?.startRecording()
        super.setUp()
    }

    override fun tearDown() {
        if (!utilizable) return
        audioRecord?.stop()
        super.tearDown()
    }

    override fun startRunning() {
        if (isRunning.get()) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "startRunning()")
        }
        isRunning.set(true)
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "stopRunning()")
        }
        isRunning.set(false)
    }

    override fun read(byteBuffer: ByteBuffer): Int {
        val result = audioRecord?.read(byteBuffer, sampleCount * 2) ?: -1
        if (0 <= result) {
            if (currentPresentationTimestamp == DEFAULT_TIMESTAMP) {
                currentPresentationTimestamp = System.nanoTime()
            } else {
                currentPresentationTimestamp += timestamp(result / 2)
            }
        } else {
            val error = when (result) {
                AudioRecord.ERROR_INVALID_OPERATION -> "ERROR_INVALID_OPERATION"
                AudioRecord.ERROR_BAD_VALUE -> "ERROR_BAD_VALUE"
                AudioRecord.ERROR_DEAD_OBJECT -> "ERROR_DEAD_OBJECT"
                AudioRecord.ERROR -> "ERROR"
                else -> "ERROR($result)"
            }
            Log.w(TAG, error)
        }
        return result
    }

    private fun timestamp(sampleCount: Int): Long {
        return (1000000.0F * (sampleCount.toFloat() / sampleRate.toFloat())).toLong()
    }

    companion object {
        const val DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT
        const val DEFAULT_SAMPLE_RATE = 44100
        const val DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.CAMCORDER
        const val DEFAULT_SAMPLE_COUNT = 1024

        private const val DEFAULT_TIMESTAMP = 0L
        private val TAG = AudioRecordSource::class.java.simpleName
    }
}
