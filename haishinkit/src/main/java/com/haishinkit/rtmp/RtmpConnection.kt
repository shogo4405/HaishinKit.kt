package com.haishinkit.rtmp

import android.os.Build
import android.util.Log
import com.haishinkit.BuildConfig
import com.haishinkit.event.Event
import com.haishinkit.event.EventDispatcher
import com.haishinkit.event.EventUtils
import com.haishinkit.event.IEventListener
import com.haishinkit.metric.FrameTracker
import com.haishinkit.net.Responder
import com.haishinkit.rtmp.messages.RtmpAudioMessage
import com.haishinkit.rtmp.messages.RtmpCommandMessage
import com.haishinkit.rtmp.messages.RtmpMessage
import com.haishinkit.rtmp.messages.RtmpMessageFactory
import com.haishinkit.rtmp.messages.RtmpVideoMessage
import org.apache.commons.lang3.StringUtils
import java.net.URI
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule

/**
 * flash.net.NetConnection for Kotlin
 */
open class RtmpConnection : EventDispatcher(null) {
    /**
     * NetStatusEvent#info.code for NetConnection
     */
    enum class Code(val rawValue: String, val level: String) {
        CALL_BAD_VERSION("NetConnection.Call.BadVersion", "error"),
        CALL_FAILED("NetConnection.Call.Failed", "error"),
        CALL_PROHIBITED("NetConnection.Call.Prohibited", "error"),
        CONNECT_APP_SHUTDOWN("NetConnection.Connect.AppShutdown", "status"),
        CONNECT_CLOSED("NetConnection.Connect.Closed", "status"),
        CONNECT_FAILED("NetConnection.Connect.Failed", "error"),
        CONNECT_IDLE_TIME_OUT("NetConnection.Connect.IdleTimeOut", "status"),
        CONNECT_INVALID_APP("NetConnection.Connect.InvalidApp", "error"),
        CONNECT_NETWORK_CHANGE("NetConnection.Connect.NetworkChange", "status"),
        CONNECT_REJECTED("NetConnection.Connect.Rejected", "status"),
        CONNECT_SUCCESS("NetConnection.Connect.Success", "status");

        fun data(description: String): Map<String, Any> {
            val data = HashMap<String, Any>()
            data.put("code", rawValue)
            data.put("level", level)
            if (StringUtils.isNoneEmpty(description)) {
                data.put("description", description)
            }
            return data
        }
    }

    enum class SupportSound(val rawValue: Short) {
        NONE(0x001),
        ADPCM(0x002),
        MP3(0x004),
        INTEL(0x008),
        UNUSED(0x0010),
        NELLY8(0x0020),
        NELLY(0x0040),
        G711A(0x0080),
        G711U(0x0100),
        AAC(0x0200),
        SPEEX(0x0800),
        ALL(0x0FFF);
    }

    enum class SupportVideo(val rawValue: Short) {
        UNUSED(0x001),
        JPEG(0x002),
        SORENSON(0x004),
        HOMEBREW(0x008),
        VP6(0x0010),
        VP6_ALPHA(0x0020),
        HOMEBREWV(0x0040),
        H264(0x0080),
        ALL(0x00FF);
    }

    enum class VideoFunction(val rawValue: Short) {
        CLIENT_SEEK(1);
    }

    private inner class EventListener(private val connection: RtmpConnection) : IEventListener {
        override fun handleEvent(event: Event) {
            val data = EventUtils.toMap(event)
            Log.i(javaClass.name, data["code"].toString())
            when (data["code"].toString()) {
                RtmpConnection.Code.CONNECT_SUCCESS.rawValue -> {
                    timerTask = Timer().schedule(0, 1000) {
                        for (stream in streams) stream.value.on()
                    }
                    val message = messageFactory.createRtmpSetChunkSizeMessage()
                    message.size = RtmpConnection.DEFAULT_CHUNK_SIZE_S
                    message.chunkStreamID = RtmpChunk.CONTROL
                    connection.socket.chunkSizeS = RtmpConnection.DEFAULT_CHUNK_SIZE_S
                    connection.doOutput(RtmpChunk.ZERO, message)
                }
            }
        }
    }

    /**
     * The URI passed to the RTMPConnection.connect() method.
     */
    var uri: URI? = null
        private set

    /**
     * The URL of .swf.
     */
    var swfUrl: String? = null

    /**
     * The URL of an HTTP referer.
     */
    var pageUrl: String? = null

    /**
     * The name of application.
     */
    var flashVer = RtmpConnection.DEFAULT_FLASH_VER

    /**
     * The outgoing RTMPChunkSize.
     */
    var chunkSize: Int
        get() = socket.chunkSizeS
        set(value) {
            socket.chunkSizeS = value
        }

    /**
     * The object encoding for this RTMPConnection instance.
     */
    var objectEncoding = RtmpConnection.DEFAULT_OBJECT_ENCODING

    /**
     * This instance connected to server(true) or not(false).
     */
    val isConnected: Boolean
        get() = socket.isConnected

    /**
     * The time to wait for TCP/IP Handshake done.
     */
    var timeout: Int
        get() = socket.timeout
        set(value) {
            socket.timeout = value
        }

    /**
     * The statistics of total incoming bytes.
     */
    val totalBytesIn: Long
        get() = socket.totalBytesIn.get()

    /**
     * The statistics of total outgoing bytes.
     */
    val totalBytesOut: Long
        get() = socket.totalBytesOut.get()

    internal val messages = ConcurrentHashMap<Short, RtmpMessage>()
    internal val streams = ConcurrentHashMap<Int, RtmpStream>()
    internal val responders = ConcurrentHashMap<Int, Responder>()
    internal val socket = RtmpSocket(this)
    internal var transactionID = 0
    internal val messageFactory = RtmpMessageFactory(4)
    private var timerTask: TimerTask? = null
        set(value) {
            timerTask?.cancel()
            field = value
        }
    private var arguments: MutableList<Any?> = mutableListOf()
    private val payloads = ConcurrentHashMap<Short, ByteBuffer>()
    private val frameTracker = FrameTracker()

    init {
        addEventListener(Event.RTMP_STATUS, EventListener(this))
    }

    open fun call(commandName: String, responder: Responder?, vararg arguments: Any) {
        if (!isConnected) {
            return
        }
        val listArguments = ArrayList<Any>(arguments.size)
        for (`object` in arguments) {
            listArguments.add(`object`)
        }
        val message = RtmpCommandMessage(objectEncoding)
        message.chunkStreamID = RtmpChunk.COMMAND
        message.streamID = 0
        message.transactionID = ++transactionID
        message.commandName = commandName
        message.arguments = listArguments
        if (responder != null) {
            responders[transactionID] = responder
        }
        doOutput(RtmpChunk.ZERO, message)
    }

    open fun connect(command: String, vararg arguments: Any?) {
        uri = URI.create(command)
        val uri = this.uri ?: return
        if (isConnected || uri.scheme != "rtmp") {
            return
        }
        val port = uri.port
        this.arguments.clear()
        arguments.forEach { value -> this.arguments.add(value) }
        socket.connect(uri.host, if (port == -1) RtmpConnection.DEFAULT_PORT else port)
    }

    /**
     * Closes the connection from the server.
     */
    open fun close() {
        if (!isConnected) {
            return
        }
        timerTask = null
        for (stream in streams) {
            stream.value.close()
            streams.remove(stream.key)
        }
        socket.close(false)
    }

    /**
     * Dispose the connection for a memory management.
     */
    open fun dispose() {
        timerTask = null
        streams.forEach {
            it.value.dispose()
        }
        streams.clear()
    }

    internal fun doOutput(chunk: RtmpChunk, message: RtmpMessage) {
        if (BuildConfig.DEBUG) {
            if (message is RtmpAudioMessage) {
                frameTracker.track(FrameTracker.TYPE_AUDIO, System.currentTimeMillis())
            } else if (message is RtmpVideoMessage) {
                frameTracker.track(FrameTracker.TYPE_VIDEO, System.currentTimeMillis())
            }
        }
        for (buffer in chunk.encode(socket, message)) {
            socket.doOutput(buffer)
        }
        messageFactory.release(message)
    }

    internal fun listen(buffer: ByteBuffer) {
        val rollback = buffer.position()
        try {
            val first = buffer.get()
            val chunkSizeC = socket.chunkSizeC
            val chunk = RtmpChunk.values().first { v -> v.rawValue.toInt() == ((first.toInt() and 0xff) shr 6) }
            val streamID = chunk.getStreamID(buffer)
            val payload: ByteBuffer
            val message: RtmpMessage
            if (chunk == RtmpChunk.THREE) {
                payload = payloads[streamID]!!
                message = messages[streamID]!!
                var remaining = payload.remaining()
                if (chunkSizeC < remaining) {
                    remaining = chunkSizeC
                }
                payload.put(buffer.array(), buffer.position(), remaining)
                buffer.position(buffer.position() + remaining)
                if (!payload.hasRemaining()) {
                    payload.flip()
                    if (VERBOSE) Log.v("$TAG#listen", message.toString())
                    message.decode(payload).execute(this)
                    messageFactory.release(message)
                    payloads.remove(streamID)
                }
            } else {
                message = chunk.decode(streamID, this, buffer)
                if (message.length <= chunkSizeC) {
                    if (VERBOSE) Log.v("$TAG#listen", message.toString())
                    message.decode(buffer).execute(this)
                    messageFactory.release(message)
                } else {
                    payload = ByteBuffer.allocate(message.length)
                    payload.put(buffer.array(), buffer.position(), chunkSizeC)
                    buffer.position(buffer.position() + chunkSizeC)
                    payloads[streamID] = payload
                }
                messages[streamID] = message
            }
        } catch (e: IndexOutOfBoundsException) {
            buffer.position(rollback)
            throw e
        }

        if (buffer.hasRemaining()) {
            listen(buffer)
        }
    }

    internal fun createStream(stream: RtmpStream) {
        call(
            "createStream",
            object : Responder {
                override fun onResult(arguments: List<Any?>) {
                    for (s in streams) {
                        if (s.value == stream) {
                            streams.remove(s.key)
                            break
                        }
                    }
                    val id = (arguments[0] as Double).toInt()
                    stream.id = id
                    streams[id] = stream
                    stream.readyState = RtmpStream.ReadyState.OPEN
                }
                override fun onStatus(arguments: List<Any?>) {
                    Log.w(javaClass.name + "#onStatus", arguments.toString())
                }
            }
        )
    }

    internal fun createConnectionMessage(): RtmpMessage {
        val paths = uri!!.path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val message = RtmpCommandMessage(RtmpObjectEncoding.AMF0)
        val commandObject = HashMap<String, Any?>()
        commandObject["app"] = paths[1]
        commandObject["flashVer"] = flashVer
        commandObject["swfUrl"] = swfUrl
        commandObject["tcUrl"] = uri!!.toString()
        commandObject["fpad"] = false
        commandObject["capabilities"] = RtmpConnection.DEFAULT_CAPABILITIES
        commandObject["audioCodecs"] = SupportSound.AAC.rawValue
        commandObject["videoCodecs"] = SupportVideo.H264.rawValue
        commandObject["videoFunction"] = VideoFunction.CLIENT_SEEK.rawValue
        commandObject["pageUrl"] = pageUrl
        commandObject["objectEncoding"] = objectEncoding.rawValue
        message.chunkStreamID = RtmpChunk.COMMAND
        message.streamID = 0
        message.commandName = "connect"
        message.transactionID = ++transactionID
        message.commandObject = commandObject
        message.arguments = arguments
        return message
    }

    companion object {
        const val DEFAULT_PORT = 1935
        const val DEFAULT_FLASH_VER = "FMLE/3.0 (compatible; FMSc/1.0)"
        val DEFAULT_OBJECT_ENCODING = RtmpObjectEncoding.AMF0

        private val TAG = RtmpConnection::class.java.simpleName
        private const val DEFAULT_CHUNK_SIZE_S = 1024 * 8
        private const val DEFAULT_CAPABILITIES = 239
        private const val VERBOSE = false
    }
}
