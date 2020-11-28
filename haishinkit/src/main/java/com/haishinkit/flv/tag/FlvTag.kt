package com.haishinkit.flv.tag

import java.nio.ByteBuffer

/**
 * An interface representations FlvTag.
 */
interface FlvTag {
    /**
     * Type of the tag.
     */
    val type: Byte

    /**
     * Length of the data in the Data field.
     */
    var dataSize: Int

    /**
     * Time in milliseconds field.
     */
    var timestamp: Int

    /**
     * Extended time in field.
     */
    var timestampExtended: Byte

    /**
     * Always 0
     */
    val streamId: Int

    /**
     * Body of tag.
     */
    var data: ByteBuffer?

    fun toByteArray(): ByteArray

    companion object {
        var TYPE_AUDIO: Byte = 8
        var TYPE_VIDEO: Byte = 9
        var TYPE_DATA: Byte = 18
    }
}
