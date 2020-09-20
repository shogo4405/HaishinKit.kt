package com.haishinkit.rtmp

import android.util.Log
import com.haishinkit.events.Event
import com.haishinkit.events.EventDispatcher
import com.haishinkit.events.IEventListener
import com.haishinkit.net.Responder
import com.haishinkit.rtmp.messages.RTMPCommandMessage
import com.haishinkit.rtmp.messages.RTMPMessage
import com.haishinkit.rtmp.messages.RTMPMessageFactory
import com.haishinkit.util.EventUtils
import org.apache.commons.lang3.StringUtils
import java.net.URI
import java.nio.ByteBuffer
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule

open class RTMPConnection : EventDispatcher(null) {

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

    private inner class EventListener(private val connection: RTMPConnection) : IEventListener {
        override fun handleEvent(event: Event) {
            val data = EventUtils.toMap(event)
            when (data["code"].toString()) {
                RTMPConnection.Code.CONNECT_SUCCESS.rawValue -> {
                    timerTask = Timer().schedule(0, 1000) {
                        for (stream in streams) stream.value.on()
                    }
                    var message = messageFactory.createRTMPSetChunkSizeMessage()
                    message.size = RTMPConnection.DEFAULT_CHUNK_SIZE_S
                    message.chunkStreamID = RTMPChunk.CONTROL
                    connection.socket.chunkSizeS = RTMPConnection.DEFAULT_CHUNK_SIZE_S
                    connection.doOutput(RTMPChunk.ZERO, message)
                }
            }
        }
    }

    var uri: URI? = null
        private set
    var swfUrl: String? = null
    var pageUrl: String? = null
    var flashVer = RTMPConnection.DEFAULT_FLASH_VER
    var objectEncoding = RTMPConnection.DEFAULT_OBJECT_ENCODING
    internal val messages = ConcurrentHashMap<Short, RTMPMessage>()
    internal val streams = ConcurrentHashMap<Int, RTMPStream>()
    internal val responders = ConcurrentHashMap<Int, Responder>()
    internal val socket = RTMPSocket(this)
    internal var transactionID = 0
    internal val messageFactory = RTMPMessageFactory(4)
    private var timerTask: TimerTask? = null
        set(value) {
            timerTask?.cancel()
            field = value
        }
    private var arguments: MutableList<Any?> = mutableListOf()
    private val payloads = ConcurrentHashMap<Short, ByteBuffer>()

    init {
        addEventListener(Event.RTMP_STATUS, EventListener(this))
    }

    val isConnected: Boolean
        get() = socket.isConnected

    fun call(commandName: String, responder: Responder?, vararg arguments: Any) {
        if (!isConnected) {
            return
        }
        val listArguments = ArrayList<Any>(arguments.size)
        for (`object` in arguments) {
            listArguments.add(`object`)
        }
        val message = RTMPCommandMessage(objectEncoding)
        message.chunkStreamID = RTMPChunk.COMMAND
        message.streamID = 0
        message.transactionID = ++transactionID
        message.commandName = commandName
        message.arguments = listArguments
        if (responder != null) {
            responders[transactionID] = responder
        }
        doOutput(RTMPChunk.ZERO, message)
    }

    fun connect(command: String, vararg arguments: Any?) {
        uri = URI.create(command)
        if (isConnected || uri!!.scheme != "rtmp") {
            return
        }
        val port = uri!!.port
        this.arguments.clear()
        arguments.forEach { value ->
            this.arguments.add(value)
        }
        socket.connect(uri!!.host, if (port == -1) RTMPConnection.DEFAULT_PORT else port)
    }

    fun close() {
        if (!isConnected) {
            return
        }
        timerTask = null
        socket.close(false)
    }

    fun dispose() {
        timerTask = null
        streams.forEach {
            it.value.dispose()
        }
        streams.clear()
    }

    internal fun doOutput(chunk: RTMPChunk, message: RTMPMessage) {
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
            var chunk = RTMPChunk.values().first { v -> v.rawValue.toInt() == ((first.toInt() and 0xff) shr 6) }
            val streamID = chunk.getStreamID(buffer)
            val payload: ByteBuffer
            val message: RTMPMessage
            if (chunk == RTMPChunk.THREE) {
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
                    message.decode(payload).execute(this)
                    messageFactory.release(message)
                    Log.v(javaClass.name + "#listen", message.toString())
                    payloads.remove(streamID)
                }
            } else {
                message = chunk.decode(streamID, this, buffer)
                if (message.length <= chunkSizeC) {
                    message.decode(buffer).execute(this)
                    messageFactory.release(message)
                    Log.v(javaClass.name + "#listen", message.toString())
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

    internal fun createStream(stream: RTMPStream) {
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
                    stream.readyState = RTMPStream.ReadyState.OPEN
                }
                override fun onStatus(arguments: List<Any?>) {
                    Log.w(javaClass.name + "#onStatus", arguments.toString())
                }
            }
        )
    }

    internal fun createConnectionMessage(): RTMPMessage {
        val paths = uri!!.path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val message = RTMPCommandMessage(RTMPObjectEncoding.AMF0)
        val commandObject = HashMap<String, Any?>()
        commandObject["app"] = paths[1]
        commandObject["flashVer"] = flashVer
        commandObject["swfUrl"] = swfUrl
        commandObject["tcUrl"] = uri!!.toString()
        commandObject["fpad"] = false
        commandObject["capabilities"] = RTMPConnection.DEFAULT_CAPABILITIES
        commandObject["audioCodecs"] = SupportSound.AAC.rawValue
        commandObject["videoCodecs"] = SupportVideo.H264.rawValue
        commandObject["videoFunction"] = VideoFunction.CLIENT_SEEK.rawValue
        commandObject["pageUrl"] = pageUrl
        commandObject["objectEncoding"] = objectEncoding.rawValue
        message.chunkStreamID = RTMPChunk.COMMAND
        message.streamID = 0
        message.commandName = "connect"
        message.transactionID = ++transactionID
        message.commandObject = commandObject
        if (arguments != null) {
            message.arguments = arguments
        }
        return message
    }

    companion object {
        const val DEFAULT_PORT = 1935
        const val DEFAULT_FLASH_VER = "FMLE/3.0 (compatible; FMSc/1.0)"
        val DEFAULT_OBJECT_ENCODING = RTMPObjectEncoding.AMF0

        private const val DEFAULT_CHUNK_SIZE_S = 1024 * 8
        private const val DEFAULT_CAPABILITIES = 239
    }
}
