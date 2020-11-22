package com.haishinkit.rtmp.messages

import com.haishinkit.amf.Amf0Deserializer
import com.haishinkit.amf.Amf0Serializer
import com.haishinkit.event.Event
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpObjectEncoding
import java.nio.ByteBuffer
import java.util.ArrayList

/**
 *  7.1.1. Command Message (20, 17)
 */
internal class RtmpCommandMessage(private val objectEncoding: RtmpObjectEncoding) : RtmpMessage(objectEncoding.commandType) {
    var commandName: String? = null
    var transactionID = 0
    var commandObject: Map<String, Any?>? = null
    var arguments: List<Any?> = mutableListOf()
    override var length: Int = CAPACITY

    override fun encode(buffer: ByteBuffer): RtmpMessage {
        if (type == RtmpMessage.Type.AMF3_COMMAND) {
            buffer.put(0x00.toByte())
        }
        val serializer = Amf0Serializer(buffer)
        serializer.putString(commandName)
        serializer.putDouble(transactionID.toDouble())
        serializer.putMap(commandObject)
        for (`object` in arguments) {
            serializer.putObject(`object`)
        }
        return this
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        val position = buffer.position()
        val deserializer = Amf0Deserializer(buffer)
        commandName = deserializer.string
        transactionID = deserializer.double.toInt()
        commandObject = deserializer.map
        val arguments = ArrayList<Any?>()
        while (buffer.position() - position != length) {
            arguments.add(deserializer.`object`)
        }
        this.arguments = arguments
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        val responders = connection.responders

        if (responders.containsKey(transactionID)) {
            val responder = responders[transactionID]
            when (commandName) {
                "_result" -> {
                    responder?.onResult(arguments)
                    return this
                }
                "_error" -> {
                    responder?.onStatus(arguments)
                    return this
                }
            }
            responders.remove(transactionID)
            return this
        }

        when (commandName) {
            "close" -> {
                connection.close()
            }
            "onStatus" -> {
                val stream = connection.streams[streamID]
                stream?.dispatchEventWith(Event.RTMP_STATUS, false, if (arguments.isEmpty()) null else arguments[0])
            }
            else -> {
                connection.dispatchEventWith(Event.RTMP_STATUS, false, if (arguments.isEmpty()) null else arguments[0])
            }
        }

        return this
    }

    companion object {
        private const val CAPACITY = 1024
    }
}
