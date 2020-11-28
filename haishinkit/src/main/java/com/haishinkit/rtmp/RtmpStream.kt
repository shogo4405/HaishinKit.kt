package com.haishinkit.rtmp

import android.util.Log
import com.haishinkit.event.Event
import com.haishinkit.event.EventDispatcher
import com.haishinkit.event.EventUtils
import com.haishinkit.event.IEventDispatcher
import com.haishinkit.event.IEventListener
import com.haishinkit.net.NetStream
import com.haishinkit.rtmp.messages.RtmpCommandMessage
import com.haishinkit.rtmp.messages.RtmpDataMessage
import com.haishinkit.rtmp.messages.RtmpMessage
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * An object that provides the interface to control a one-way channel over a RTMPConnection.
 */
open class RtmpStream(internal var connection: RtmpConnection) : NetStream(), IEventDispatcher {
    enum class HowToPublish(val rawValue: String) {
        RECORD("record"),
        APPEND("append"),
        APPEND_WITH_GAP("appendWithGap"),
        LIVE("live");
    }

    enum class Code(val rawValue: String, private val level: String) {
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

    interface Listener : NetStream.Listener {
        fun onStatics(stream: RtmpStream, connection: RtmpConnection)
    }

    internal inner class EventListener(private val stream: RtmpStream) : IEventListener {
        override fun handleEvent(event: Event) {
            val data = EventUtils.toMap(event)
            when (data["code"].toString()) {
                RtmpConnection.Code.CONNECT_SUCCESS.rawValue -> {
                    connection.createStream(stream)
                }
                RtmpStream.Code.PUBLISH_START.rawValue -> {
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
        CLOSED(0x06)
    }

    var listener: Listener? = null

    @Volatile var currentFPS: Int = 0
        private set

    internal var id = 0
    internal var readyState = ReadyState.INITIALIZED
        set(value) {
            Log.d(TAG, value.toString())
            when (field) {
                RtmpStream.ReadyState.PUBLISHING -> {
                    if (audio != null) {
                        audioCodec.stopRunning()
                        audio?.stopRunning()
                    }
                    if (video != null) {
                        videoCodec.stopRunning()
                        video?.stopRunning()
                    }
                }
                else -> {
                }
            }
            field = value
            when (value) {
                RtmpStream.ReadyState.OPEN -> {
                    currentFPS = 0
                    frameCount.set(0)
                    for (message in messages) {
                        message.streamID = id
                        if (message is RtmpCommandMessage) {
                            message.transactionID = ++connection.transactionID
                        }
                        connection.doOutput(RtmpChunk.ZERO, message)
                    }
                    messages.clear()
                }
                RtmpStream.ReadyState.PUBLISHING -> {
                    send("@setDataFrame", "onMetaData", toMetaData())
                    muxer.clear()
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
    internal val messages = ArrayList<RtmpMessage>()
    internal var frameCount = AtomicInteger(0)
    private val dispatcher: EventDispatcher by lazy {
        EventDispatcher(this)
    }
    private var muxer = RtmpMuxer(this)
    private val eventListener = EventListener(this)

    init {
        val count = (connection.streams.count() * -1) - 1
        connection.streams[count] = this
        connection.addEventListener(Event.RTMP_STATUS, eventListener)
        if (connection.isConnected) {
            connection.createStream(this)
        }
        addEventListener(Event.RTMP_STATUS, eventListener)
        audioCodec.listener = muxer
        videoCodec.listener = muxer
    }

    open fun publish(name: String?, howToPublish: HowToPublish = HowToPublish.LIVE) {
        val message = RtmpCommandMessage(connection.objectEncoding)
        message.transactionID = 0
        message.commandName = if (name != null) "publish" else "closeStream"
        message.chunkStreamID = RtmpChunk.AUDIO
        message.streamID = id

        if (name == null) {
            when (readyState) {
                RtmpStream.ReadyState.PUBLISHING -> connection.doOutput(RtmpChunk.ZERO, message)
                else -> {}
            }
            return
        }

        val arguments = mutableListOf<Any?>()
        arguments.add(name)
        arguments.add(howToPublish.rawValue)
        message.arguments = arguments

        when (readyState) {
            RtmpStream.ReadyState.INITIALIZED, RtmpStream.ReadyState.CLOSED -> {
                messages.add(message)
            }
            RtmpStream.ReadyState.OPEN -> {
                connection.doOutput(RtmpChunk.ZERO, message)
                readyState = ReadyState.PUBLISH
            }
            else -> {}
        }
    }

    open fun play(vararg arguments: Any) {
        val streamName = if (arguments.isEmpty()) null else arguments[0]
        val message = RtmpCommandMessage(connection.objectEncoding)
        message.transactionID = 0
        message.commandName = if (streamName != null) "play" else "closeStream"
        message.arguments = listOf(*arguments)
        message.chunkStreamID = RtmpChunk.CONTROL
        message.streamID = id

        if (streamName == null) {
            when (readyState) {
                RtmpStream.ReadyState.PLAYING -> {
                    connection.doOutput(RtmpChunk.ZERO, message)
                }
                else -> {}
            }
            return
        }

        when (readyState) {
            RtmpStream.ReadyState.INITIALIZED, RtmpStream.ReadyState.CLOSED -> {
                messages.add(message)
            }
            RtmpStream.ReadyState.OPEN, RtmpStream.ReadyState.PLAYING -> {
                connection.doOutput(RtmpChunk.ZERO, message)
            }
            else -> {
            }
        }
    }

    /**
     * Sends a message on a published stream.
     */
    open fun send(handlerName: String, vararg arguments: Any) {
        readyState == ReadyState.INITIALIZED || readyState == ReadyState.CLOSED
        val message = RtmpDataMessage(connection.objectEncoding)
        message.handlerName = handlerName
        arguments.forEach { value ->
            message.arguments.add(value)
        }
        message.streamID = id
        message.chunkStreamID = RtmpChunk.COMMAND
        connection.doOutput(RtmpChunk.ZERO, message)
    }

    open override fun close() {
        if (readyState == ReadyState.CLOSED) {
            return
        }
        readyState = ReadyState.CLOSED
        val message = RtmpCommandMessage(RtmpObjectEncoding.AMF0)
        message.streamID = 0
        message.chunkStreamID = RtmpChunk.COMMAND
        message.commandName = "deleteStream"
        message.arguments = listOf<Any>(id)
        connection.doOutput(RtmpChunk.ZERO, message)
    }

    open fun dispose() {
        connection.removeEventListener(Event.RTMP_STATUS, eventListener)
        audio?.tearDown()
        audioCodec.dispose()
        video?.tearDown()
        videoCodec.dispose()
    }

    override fun addEventListener(type: String, listener: IEventListener, useCapture: Boolean) {
        dispatcher.addEventListener(type, listener, useCapture)
    }

    override fun dispatchEvent(event: Event) {
        dispatcher.dispatchEvent(event)
    }

    override fun dispatchEventWith(type: String, bubbles: Boolean, data: Any?) {
        dispatcher.dispatchEventWith(type, bubbles, data)
    }

    override fun removeEventListener(type: String, listener: IEventListener, useCapture: Boolean) {
        dispatcher.removeEventListener(type, listener, useCapture)
    }

    internal fun on() {
        currentFPS = frameCount.get()
        frameCount.set(0)
        listener?.onStatics(this, connection)
    }

    private fun toMetaData(): Map<String, Any> {
        val metadata = mutableMapOf<String, Any>()
        if (video != null) {
            metadata["width"] = video?.resolution?.width ?: 0
            metadata["height"] = video?.resolution?.height ?: 0
            metadata["framerate"] = videoCodec.frameRate
            metadata["videocodecid"] = com.haishinkit.flv.FlvVideoCodec.AVC.toInt()
            metadata["videodatarate"] = videoCodec.bitRate / 1000
        }
        if (audio != null) {
            metadata["audiocodecid"] = com.haishinkit.flv.FlvAudioCodec.AAC.toInt()
            metadata["audiodatarate"] = audioCodec.bitRate / 1000
        }
        return metadata
    }

    companion object {
        private val TAG = RtmpStream::class.java.simpleName
    }
}
