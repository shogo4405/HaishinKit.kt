package com.haishinkit.net

import com.haishinkit.codec.AudioCodec
import com.haishinkit.codec.VideoCodec
import com.haishinkit.media.AudioSource
import com.haishinkit.media.VideoSource
import com.haishinkit.view.NetStreamView
import org.apache.commons.lang3.builder.ToStringBuilder
import kotlin.properties.Delegates

/**
 * The `NetStream` class is the foundation of a RTMPStream.
 */
open abstract class NetStream {
    class AudioSettings(private var stream: NetStream?) {
        var channelCount: Int by Delegates.observable(AudioCodec.DEFAULT_CHANNEL_COUNT) { _, _, newValue ->
            stream?.audioCodec?.channelCount = newValue
        }
        var bitRate: Int by Delegates.observable(AudioCodec.DEFAULT_BIT_RATE) { _, _, newValue ->
            stream?.audioCodec?.bitRate = newValue
        }
        var sampleRate: Int by Delegates.observable(AudioCodec.DEFAULT_SAMPLE_RATE) { _, _, newValue ->
            stream?.audioCodec?.sampleRate = newValue
        }
        fun dispose() {
            stream = null
        }
        override fun toString(): String {
            return ToStringBuilder.reflectionToString(this)
        }
    }

    class VideoSettings(private var stream: NetStream?) {
        var width: Int by Delegates.observable(VideoCodec.DEFAULT_WIDTH) { _, _, newValue ->
            stream?.videoCodec?.width = newValue
        }
        var height: Int by Delegates.observable(VideoCodec.DEFAULT_HEIGHT) { _, _, newValue ->
            stream?.videoCodec?.height = newValue
        }
        var bitRate: Int by Delegates.observable(VideoCodec.DEFAULT_BIT_RATE) { _, _, newValue ->
            stream?.videoCodec?.bitRate = newValue
        }
        var IFrameInterval: Int by Delegates.observable(VideoCodec.DEFAULT_I_FRAME_INTERVAL) { _, _, newValue ->
            stream?.videoCodec?.IFrameInterval = newValue
        }
        var frameRate: Int by Delegates.observable(VideoCodec.DEFAULT_FRAME_RATE) { _, _, newValue ->
            stream?.videoCodec?.frameRate = newValue
        }
        fun dispose() {
            stream = null
        }
        override fun toString(): String {
            return ToStringBuilder.reflectionToString(this)
        }
    }

    val videoSetting: VideoSettings by lazy {
        VideoSettings(this)
    }

    val audioSetting: AudioSettings by lazy {
        AudioSettings(this)
    }

    internal val audioCodec = AudioCodec()
    internal val videoCodec = VideoCodec()
    internal var renderer: NetStreamView? = null
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
}
