package com.haishinkit.flv.tag

import com.haishinkit.flv.FlvAvcPacketType
import com.haishinkit.flv.FlvFlameType
import com.haishinkit.flv.FlvVideoCodec
import java.nio.ByteBuffer

data class FlvVideoTag(
    override val type: Byte = FlvTag.TYPE_VIDEO,
    override var dataSize: Int,
    override var timestamp: Int,
    override var timestampExtended: Byte,
    override val streamId: Int = 0,
    override var data: ByteBuffer? = null,
    var frameType: Byte = FlvFlameType.COMMAND,
    var codec: Byte = FlvVideoCodec.UNKNOWN,
    var avcPacketType: Byte = FlvAvcPacketType.EOS,
    var compositionTime: Int = 0
) : FlvTag {
    override fun toByteArray(): ByteArray {
        return byteArrayOf()
    }
}
