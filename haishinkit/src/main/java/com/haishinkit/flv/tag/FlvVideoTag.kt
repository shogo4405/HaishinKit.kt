package com.haishinkit.flv.tag

import com.haishinkit.flv.FlvAvcPacketType
import com.haishinkit.flv.FlvFlameType
import com.haishinkit.flv.FlvVideoCodec
import java.nio.ByteBuffer

data class FlvVideoTag(
    override val type: Int = FlvTag.TYPE_VIDEO,
    override var dataSize: Long,
    override var timestamp: Long,
    override var timestampExtended: Int,
    override var streamId: Long,
    override var offset: Long,
    override var payload: ByteBuffer?,
    var frameType: Byte = FlvFlameType.COMMAND,
    var codec: Byte = FlvVideoCodec.UNKNOWN,
    var avcPacketType: Byte = FlvAvcPacketType.EOS,
    var compositionTime: Int = 0
) : FlvTag {
    override fun toByteArray(): ByteArray {
        return byteArrayOf()
    }
}
