package com.haishinkit.media

import com.haishinkit.lang.Running
import java.nio.ByteBuffer

/**
 * An interface that captures a source.
 */
interface Source : Running {
    var stream: Stream?
    val currentPresentationTimestamp: Long
        get() = 0

    fun read(byteBuffer: ByteBuffer): Int {
        return -1
    }
}
