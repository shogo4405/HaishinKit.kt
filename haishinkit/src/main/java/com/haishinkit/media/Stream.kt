package com.haishinkit.media

import android.content.Context
import android.graphics.Rect
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
import com.haishinkit.screen.Screen

/**
 * The NetStream class is the foundation of a RtmpStream.
 */
@Suppress("UNUSED")
abstract class Stream(applicationContext: Context) {
    /**
     * The offscreen renderer for video output.
     */
    val screen: Screen by lazy {
        val screen =
            Screen.create(applicationContext).apply {
                frame = Rect(0, 0, DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT)
            }
        videoCodec.pixelTransform.screen = screen
        screen
    }

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
            view?.videoEffect = value ?: DefaultVideoEffect.shared
            field = value
        }

    /**
     * Specifies the StreamView object.
     */
    var view: StreamView? = null

    /**
     * The current audioSource object.
     */
    var audioSource: AudioSource? = null
        internal set(value) {
            field?.stopRunning()
            field?.stream = null
            field = value
            field?.stream = this
            field?.startRunning()
        }

    /**
     * The current videoSource object.
     */
    var videoSource: VideoSource? = null
        internal set(value) {
            field?.stopRunning()
            screen.removeChild(field?.screen)
            field?.stream = null
            field = value
            field?.stream = this
            screen.addChild(field?.screen)
            field?.startRunning()
        }

    internal val audioCodec by lazy { AudioCodec() }
    internal val videoCodec by lazy { VideoCodec(applicationContext) }

    /**
     * Attaches an audio stream to a NetStream.
     */
    fun attachAudio(audio: AudioSource?) {
        if (audio == null) {
            this.audioSource = null
            return
        }
        this.audioSource = audio
    }

    /**
     * Attaches a video stream to a NetStream.
     */
    fun attachVideo(video: VideoSource?) {
        if (video == null) {
            this.videoSource = null
            return
        }
        this.videoSource = video
    }

    /**
     * Closes the stream from the server.
     */
    abstract fun close()

    /**
     * Disposes the stream of memory management.
     */
    open fun dispose() {
        audioCodec.dispose()
        audioSource = null
        videoCodec.dispose()
        videoSource = null
        view = null
        screen.dispose()
    }

    internal fun createAudioTrack(mediaFormat: MediaFormat): AudioTrack {
        val sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val channelMask =
            if (channelCount == 2) {
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
                            .build(),
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelMask)
                            .build(),
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
                    AudioTrack.MODE_STREAM,
                )
            }
        } catch (e: Exception) {
            return AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                channelMask,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM,
            )
        }
    }

    companion object {
        private val TAG = Stream::class.java.simpleName

        const val DEFAULT_SCREEN_WIDTH = 1280
        const val DEFAULT_SCREEN_HEIGHT = 720
    }
}
