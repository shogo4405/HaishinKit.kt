package com.haishinkit.rtmp.messages

import com.haishinkit.rtmp.RtmpConnection
import java.nio.ByteBuffer

internal open class RtmpAudioMessage : RtmpMessage(RtmpMessage.Type.AUDIO) {
    var codec: Byte = 0
    var soundRate: Byte = 0
    var soundSize: Byte = 0
    var soundType: Byte = 0
    var payload: ByteBuffer? = null

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        val first = buffer.get()
        codec = (first.toInt() shr 4).toByte()
        soundRate = (first.toInt() and (12 shr 2)).toByte()
        soundSize = (first.toInt() and (2 shr 1)).toByte()
        soundType = (first.toInt() and 1).toByte()
        val payload = ByteArray(length - 1)
        buffer.get(payload)
        this.payload = ByteBuffer.wrap(payload)
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        return this
    }
}
