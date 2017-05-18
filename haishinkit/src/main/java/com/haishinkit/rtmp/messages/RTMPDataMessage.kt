package com.haishinkit.rtmp.messages

import com.haishinkit.amf.AMF0Deserializer
import com.haishinkit.amf.AMF0Serializer
import com.haishinkit.rtmp.RTMPConnection
import com.haishinkit.rtmp.RTMPObjectEncoding
import com.haishinkit.rtmp.RTMPSocket

import java.nio.ByteBuffer
import java.util.ArrayList

/**
 * 7.1.2. Data Message (18, 15)
 */
internal class RTMPDataMessage(val objectEncoding: RTMPObjectEncoding) : RTMPMessage(objectEncoding.dataType) {
    var handlerName: String? = null
    var arguments: ArrayList<Any?> = ArrayList()

    override fun encode(socket: RTMPSocket): ByteBuffer {
        val buffer = ByteBuffer.allocate(1024)
        val serializer = AMF0Serializer(buffer)
        serializer.putString(handlerName)
        if (!arguments.isEmpty()) {
            for (argument in arguments) {
                serializer.putObject(argument)
            }
        }
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RTMPMessage {
        val eom = buffer.position() + length
        val deserializer = AMF0Deserializer(buffer)
        handlerName = deserializer.string
        val arguments = arguments
        while (buffer.position() < eom) {
            arguments.add(deserializer.`object`)
        }
        return this
    }

    override fun execute(connection: RTMPConnection): RTMPMessage {
        return this
    }
}
