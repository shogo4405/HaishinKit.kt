package com.haishinkit.rtmp.messages

import java.util.ArrayList
import java.nio.ByteBuffer

import com.haishinkit.amf.AMF0Deserializer
import com.haishinkit.amf.AMF0Serializer
import com.haishinkit.events.Event
import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPObjectEncoding
import com.haishinkit.rtmp.RTMPSocket
import com.haishinkit.util.Log

/**
 *  7.1.1. Command Message (20, 17)
 */
internal class RTMPCommandMessage(val objectEncoding: RTMPObjectEncoding) : RTMPMessage(objectEncoding.commandType) {
    var commandName: String? = null
    var transactionID = 0
    var commandObject: Map<String, Any?>? = null
    var arguments: List<Any?> = mutableListOf()

    override fun encode(socket: RTMPSocket): ByteBuffer {
        val buffer = ByteBuffer.allocate(CAPACITY)
        if (type == RTMPMessage.Type.AMF3_COMMAND) {
            buffer.put(0x00.toByte())
        }
        val serializer = AMF0Serializer(buffer)
        serializer.putString(commandName)
        serializer.putDouble(transactionID.toDouble())
        serializer.putMap(commandObject)
        for (`object` in arguments) {
            serializer.putObject(`object`)
        }
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RTMPMessage {
        val position = buffer.position()
        val deserializer = AMF0Deserializer(buffer)
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

    override fun execute(connection: RTMPConnection): RTMPMessage {
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
        private val CAPACITY = 1024
    }
}
