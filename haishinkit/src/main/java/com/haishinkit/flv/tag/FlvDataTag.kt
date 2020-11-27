package com.haishinkit.flv.tag

import java.nio.ByteBuffer

data class FlvDataTag(
    override val type: Int = FlvTag.TYPE_DATA,
    override var dataSize: Long,
    override var timestamp: Long,
    override var timestampExtended: Int,
    override var streamId: Long,
    override var offset: Long,
    override var payload: ByteBuffer?,
) : FlvTag {
    override fun toByteArray(): ByteArray {
        return byteArrayOf()
    }
}
