package com.haishinkit.flv.tag

import java.nio.ByteBuffer

/**
 * An interface representations FlvTag.
 */
interface FlvTag {
    var type: Int
    var dataSize: Long
    var timestamp: Long
    var timestampExtended: Int
    var streamId: Long
    var offset: Long
    var payload: ByteBuffer?

    fun toByteArray(): ByteArray

    companion object {
        var TYPE_AUDIO = 8
        var TYPE_VIDEO = 9
        var TYPE_DATA = 18
    }
}
