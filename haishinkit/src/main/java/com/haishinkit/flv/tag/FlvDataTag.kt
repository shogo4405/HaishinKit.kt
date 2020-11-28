package com.haishinkit.flv.tag

import java.nio.ByteBuffer

data class FlvDataTag(
    override val type: Byte = FlvTag.TYPE_DATA,
    override var dataSize: Int,
    override var timestamp: Int,
    override var timestampExtended: Byte,
    override val streamId: Int = 0,
    override var data: ByteBuffer? = null,
) : FlvTag {
    override fun toByteArray(): ByteArray {
        return byteArrayOf()
    }
}
