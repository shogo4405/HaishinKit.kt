package com.haishinkit.net

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import com.haishinkit.codec.AudioCodec
import com.haishinkit.codec.VideoCodec
import com.haishinkit.graphics.effect.DefaultVideoEffect
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.media.AudioSource
import com.haishinkit.media.VideoSource
import com.haishinkit.view.NetStreamDrawable

/**
 * The NetStream class is the foundation of a RtmpStream.
 */
abstract class NetStream {
    /**
     * Specifies the video codec settings.
     */
    val videoSetting: VideoCodec.Setting by lazy {
        VideoCodec.Setting(videoCodec)
    }

    /**
     * Specifies the audio codec settings.
     */
    val audioSetting: AudioCodec.Setting by lazy {
        AudioCodec.Setting(audioCodec)
    }

    /**
     * Specifies the videoEffect such as a monochrome, a sepia.
     */
    var videoEffect: VideoEffect? = null
        set(value) {
            videoCodec.pixelTransform.videoEffect = value ?: DefaultVideoEffect.shared
            drawable?.videoEffect = value ?: DefaultVideoEffect.shared
            field = value
        }

    /**
     * Specifies the deviceOrientation that is current phone device orientation.
     */
    var deviceOrientation: Int = 0
        set(value) {
            videoCodec.pixelTransform.deviceOrientation = value
            drawable?.deviceOrientation = value
            field = value
        }

    var drawable: NetStreamDrawable? = null

    internal val audioCodec = AudioCodec()
    internal val videoCodec = VideoCodec()
    internal var audio: AudioSource? = null
    internal var video: VideoSource? = null

    /**
     * Attaches an audio stream to a NetStream.
     */
    open fun attachAudio(audio: AudioSource?) {
        if (audio == null) {
            this.audio?.tearDown()
            this.audio = null
            return
        }
        this.audio = audio
        this.audio?.stream = this
        this.audio?.setUp()
    }

    /**
     * Attaches a video stream to a NetStream.
     */
    open fun attachVideo(video: VideoSource?) {
        if (video == null) {
            this.video?.tearDown()
            this.video = null
            return
        }
        this.video = video
        this.video?.stream = this
        this.video?.setUp()
    }

    /**
     * Closes the stream from the server.
     */
    abstract fun close()

    /**
     * Disposes the stream of memory management.
     */
    open fun dispose() {
        audio?.tearDown()
        audioCodec.dispose()
        video?.tearDown()
        videoCodec.dispose()
        drawable?.dispose()
    }

    internal fun createAudioTrack(mediaFormat: MediaFormat): AudioTrack {
        val sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val channelMask = if (channelCount == 2) {
            AudioFormat.CHANNEL_OUT_STEREO
        } else {
            AudioFormat.CHANNEL_OUT_MONO
        }
        val bufferSize =
            AudioTrack.getMinBufferSize(sampleRate, channelCount, AudioFormat.ENCODING_PCM_16BIT)
        Log.d(TAG, "sampleRate=$sampleRate, channelCount=$channelCount, bufferSize=$bufferSize")
        try {
            return if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelMask)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .apply {
                        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
                            setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                        }
                    }.build()
            } else {
                return AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    channelMask,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }
        } catch (e: Exception) {
            return AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                channelMask,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )
        }
    }

    companion object {
        private val TAG = NetStream::class.java.simpleName
    }
}
