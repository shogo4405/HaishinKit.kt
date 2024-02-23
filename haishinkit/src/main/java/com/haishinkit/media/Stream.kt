package com.haishinkit.media

import android.content.Context
import com.haishinkit.codec.AudioCodec
import com.haishinkit.codec.VideoCodec
import com.haishinkit.graphics.effect.DefaultVideoEffect
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.screen.Screen

/**
 * The Stream class is the foundation of a RtmpStream.
 */
@Suppress("UNUSED")
abstract class Stream(applicationContext: Context) {
    /**
     * The offscreen renderer for video output.
     */
    val screen: Screen by lazy {
        val screen =
            Screen.create(applicationContext)
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
     * The current audio source object.
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
     * The current video source object.
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
     * Attaches an audio stream to a Stream.
     */
    fun attachAudio(audio: AudioSource?) {
        if (audio == null) {
            this.audioSource = null
            return
        }
        this.audioSource = audio
    }

    /**
     * Attaches a video stream to a Stream.
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

    private companion object {
        private val TAG = Stream::class.java.simpleName
    }
}
