package com.haishinkit.rtmp

import android.util.Log
import com.haishinkit.codec.AudioCodec
import com.haishinkit.codec.BufferInfo
import com.haishinkit.codec.BufferType
import com.haishinkit.codec.VideoCodec
import com.haishinkit.events.Event
import com.haishinkit.events.EventDispatcher
import com.haishinkit.events.IEventListener
import com.haishinkit.media.AudioSource
import com.haishinkit.media.VideoSource
import com.haishinkit.rtmp.messages.RTMPCommandMessage
import com.haishinkit.rtmp.messages.RTMPDataMessage
import com.haishinkit.rtmp.messages.RTMPMessage
import com.haishinkit.util.EventUtils
import com.haishinkit.view.CameraView
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates

open class RTMPStream(internal var connection: RTMPConnection) : EventDispatcher(null) {
    enum class HowToPublish(val rawValue: String) {
        RECORD("record"),
        APPEND("append"),
        APPEND_WITH_GAP("appendWithGap"),
        LIVE("live");
    }

    enum class Code(val rawValue: String, val level: String) {
        BUFFER_EMPTY("NetStream.Buffer.Empty", "status"),
        BUFFER_FLUSH("NetStream.Buffer.Flush", "status"),
        BUFFER_FULL("NetStream.Buffer.Full", "status"),
        CONNECT_CLOSED("NetStream.Connect.Closed", "status"),
        CONNECT_FAILED("NetStream.Connect.Failed", "error"),
        CONNECT_REJECTED("NetStream.Connect.Rejected", "error"),
        CONNECT_SUCCESS("NetStream.Connect.Success", "status"),
        DRM_UPDATE_NEEDED("NetStream.DRM.UpdateNeeded", "status"),
        FAILED("NetStream.Failed", "error"),
        MULTI_STREAM_RESET("NetStream.MulticastStream.Reset", "status"),
        PAUSE_NOTIFY("NetStream.Pause.Notify", "status"),
        PLAY_FAILED("NetStream.Play.Failed", "error"),
        PLAY_FILE_STRUCTURE_INVALID("NetStream.Play.FileStructureInvalid", "error"),
        PLAY_INSUFFICIENT_BW("NetStream.Play.InsufficientBW", "status"),
        PLAY_NO_SUPPORTED_TRACK_FOUND("NetStream.Play.NoSupportedTrackFound", "status"),
        PLAY_REST("NetStream.Play.Reset", "status"),
        PLAY_START("NetStream.Play.Start", "status"),
        PLAY_STOP("NetStream.Play.Stop", "status"),
        PLAY_STREAM_NOT_FOUND("NetStream.Play.StreamNotFound", "error"),
        PLAY_TRANSITION("NetStream.Play.Transition", "status"),
        PLAY_UNPUBLISH_NOTIFY("NetStream.Play.UnpublishNotify", "status"),
        PUBLISH_BAD_NAME("NetStream.Publish.BadName", "status"),
        PUBLISH_IDLE("NetStream.Publish.Idle", "status"),
        PUBLISH_START("NetStream.Publish.Start", "status"),
        RECORD_ALREADY_EXISTS("NetStream.Record.AlreadyExists", "status"),
        RECORD_FAILED("NetStream.Record.Failed", "status"),
        RECORD_NO_ACCESS("NetStream.Record.NoAccess", "error"),
        RECORD_START("NetStream.Record.Start", "status"),
        RECORD_STOP("NetStream.Record.Stop", "status"),
        RECORD_DISK_QUOTA_EXCEEDED("NetStream.Record.DiskQuotaExceeded", "error"),
        SECOND_SCREEN_START("NetStream.SecondScreen.Start", "status"),
        SECOND_SCREEN_STOP("NetStream.SecondScreen.Stop", "status"),
        SEEK_FAILDED("NetStream.Seek.Failed", "error"),
        SEEK_INVALID_TIME("NetStream.Seek.InvalidTime", "error"),
        SEEK_NOTIFY("NetStream.Seek.Notify", "status"),
        STEP_NOTIFY("NetStream.Step.Notify", "status"),
        UNPAUSE_NOTIFY("NetStream.Unpause.Notify", "status"),
        UNPUBLISH_SUCCESS("NetStream.Unpublish.Success", "status"),
        VIDEO_DIMENSION_CHANGE("NetStream.Video.DimensionChange", "status");

        fun data(description: String): Map<String, Any> {
            val data = HashMap<String, Any>()
            data["code"] = rawValue
            data["level"] = level
            if (StringUtils.isNoneEmpty(description)) {
                data["description"] = description
            }
            return data
        }
    }

    class AudioSettings(private var stream: RTMPStream?) {
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

    class VideoSettings(private var stream: RTMPStream?) {
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

    inner class EventListener internal constructor(private val stream: RTMPStream) : IEventListener {
        override fun handleEvent(event: Event) {
            val data = EventUtils.toMap(event)
            when (data["code"].toString()) {
                RTMPConnection.Code.CONNECT_SUCCESS.rawValue -> {
                    connection.createStream(stream)
                }
                RTMPStream.Code.PUBLISH_START.rawValue -> {
                    stream.readyState = ReadyState.PUBLISHING
                }
                else -> {
                }
            }
        }

        override fun toString(): String {
            return ToStringBuilder.reflectionToString(this)
        }
    }

    internal enum class ReadyState(val rawValue: Byte) {
        INITIALIZED(0x00),
        OPEN(0x01),
        PLAY(0x02),
        PLAYING(0x03),
        PUBLISH(0x04),
        PUBLISHING(0x05),
        CLOSED(0x06);
    }
    val videoSetting: VideoSettings by lazy {
        VideoSettings(this)
    }
    val audioSetting: AudioSettings by lazy {
        AudioSettings(this)
    }
    @Volatile var currentFPS: Int = 0
        private set

    internal var id = 0
    internal var video: VideoSource? = null
    internal var readyState = ReadyState.INITIALIZED
        set(value) {
            Log.d(javaClass.name, value.toString())
            when (field) {
                RTMPStream.ReadyState.PUBLISHING -> {
                    audioCodec.stopRunning()
                    videoCodec.stopRunning()
                    audio?.stopRunning()
                    video?.stopRunning()
                }
                else -> {
                }
            }
            field = value
            when (value) {
                RTMPStream.ReadyState.OPEN -> {
                    currentFPS = 0
                    frameCount.set(0)
                    for (message in messages) {
                        message.streamID = id
                        if (message is RTMPCommandMessage) {
                            message.transactionID = ++connection.transactionID
                        }
                        connection.doOutput(RTMPChunk.ZERO, message)
                    }
                    messages.clear()
                }
                RTMPStream.ReadyState.PUBLISHING -> {
                    send("@setDataFrame", "onMetaData", toMetaData())
                    if (audio != null) {
                        audio?.startRunning()
                        audioCodec.startRunning()
                    }
                    if (video != null) {
                        video?.startRunning()
                        videoCodec.startRunning()
                    }
                }
                else -> {
                }
            }
        }
    internal val audioCodec = AudioCodec()
    internal val videoCodec = VideoCodec()
    internal val messages = ArrayList<RTMPMessage>()
    internal var frameCount = AtomicInteger(0)
    internal var renderer: CameraView? = null
    private var muxer = RTMPMuxer(this)
    private var audio: AudioSource? = null
    private val listener = EventListener(this)

    init {
        val count = (connection.streams.count() * -1) - 1
        this.connection = connection
        this.connection.streams[count] = this
        this.connection.addEventListener(Event.RTMP_STATUS, listener)
        if (this.connection.isConnected) {
            this.connection.createStream(this)
        }
        addEventListener(Event.RTMP_STATUS, listener)
        audioCodec.listener = muxer
        videoCodec.listener = muxer
    }

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

    open fun attachCamera(video: VideoSource?) {
        if (video == null) {
            this.video?.tearDown()
            this.video = null
            return
        }
        this.video = video
        this.video?.stream = this
        this.video?.setUp()
    }

    open fun publish(name: String?, howToPublish: HowToPublish = HowToPublish.LIVE) {
        val message = RTMPCommandMessage(connection.objectEncoding)
        message.transactionID = 0
        message.commandName = if (name != null) "publish" else "closeStream"
        message.chunkStreamID = RTMPChunk.AUDIO
        message.streamID = id

        if (name == null) {
            when (readyState) {
                RTMPStream.ReadyState.PUBLISHING -> connection.doOutput(RTMPChunk.ZERO, message)
                else -> {}
            }
            return
        }

        var arguments = mutableListOf<Any?>()
        arguments.add(name)
        arguments.add(howToPublish.rawValue)
        message.arguments = arguments

        when (readyState) {
            RTMPStream.ReadyState.INITIALIZED -> {
                messages.add(message)
            }
            RTMPStream.ReadyState.OPEN -> {
                connection.doOutput(RTMPChunk.ZERO, message)
                readyState = ReadyState.PUBLISH
            }
            else -> {}
        }
    }

    open fun play(vararg arguments: Any) {
        val streamName = if (arguments.isEmpty()) null else arguments[0]
        val message = RTMPCommandMessage(connection.objectEncoding)
        message.transactionID = 0
        message.commandName = if (streamName != null) "play" else "closeStream"
        message.arguments = listOf(*arguments)
        message.chunkStreamID = RTMPChunk.CONTROL
        message.streamID = id

        if (streamName == null) {
            when (readyState) {
                RTMPStream.ReadyState.PLAYING -> {
                    connection.doOutput(RTMPChunk.ZERO, message)
                }
                else -> {}
            }
            return
        }

        when (readyState) {
            RTMPStream.ReadyState.INITIALIZED -> {
                messages.add(message)
            }
            RTMPStream.ReadyState.OPEN, RTMPStream.ReadyState.PLAYING -> {
                connection.doOutput(RTMPChunk.ZERO, message)
            }
            else -> {
            }
        }
    }

    open fun send(handlerName: String, vararg arguments: Any) {
        readyState == ReadyState.INITIALIZED || readyState == ReadyState.CLOSED ?: return
        var message = RTMPDataMessage(connection.objectEncoding)
        message.handlerName = handlerName
        arguments.forEach { value ->
            message.arguments.add(value)
        }
        message.streamID = id
        message.chunkStreamID = RTMPChunk.COMMAND
        connection.doOutput(RTMPChunk.ZERO, message)
    }

    open fun appendBytes(bytes: ByteArray?, info: BufferInfo) {
        bytes ?: return
        if (readyState != ReadyState.PUBLISHING) { return }
        when (info.type) {
            BufferType.AUDIO -> {
                audioCodec.appendBytes(bytes, info)
            }
            BufferType.VIDEO -> {
                videoCodec.appendBytes(bytes, info)
            }
        }
    }

    /**
     * Closes the stream from the server.
     */
    open fun close() {
        if (readyState == ReadyState.CLOSED) {
            return
        }
        readyState = ReadyState.CLOSED
        val message = RTMPCommandMessage(RTMPObjectEncoding.AMF0)
        message.streamID = 0
        message.chunkStreamID = RTMPChunk.COMMAND
        message.commandName = "deleteStream"
        message.arguments = listOf<Any>(id)
        connection.doOutput(RTMPChunk.ZERO, message)
    }

    open fun dispose() {
        connection.removeEventListener(Event.RTMP_STATUS, listener)
        audio?.tearDown()
        video?.tearDown()
        audioSetting.dispose()
        videoSetting.dispose()
    }

    internal fun on() {
        currentFPS = frameCount.get()
        frameCount.set(0)
    }

    private fun toMetaData(): Map<String, Any> {
        return HashMap<String, Any>()
    }
}
