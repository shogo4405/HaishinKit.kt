package com.haishinkit.media

import java.nio.ByteBuffer

/**
 * An interface that captures an audio source.
 */
interface AudioSource : Source {
    /**
     * Reads an audio buffer from this instance.
     */
    fun read(byteBuffer: ByteBuffer): Int {
        return -1
    }
}
