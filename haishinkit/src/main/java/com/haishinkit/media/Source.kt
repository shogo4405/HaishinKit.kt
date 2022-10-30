package com.haishinkit.media

import com.haishinkit.lang.Running
import com.haishinkit.lang.Utilize
import com.haishinkit.net.NetStream
import java.nio.ByteBuffer

/**
 * An interface that captures a source.
 */
interface Source : Running, Utilize {
    var stream: NetStream?
    val currentPresentationTimestamp: Long
        get() = 0

    fun read(byteBuffer: ByteBuffer): Int {
        return -1
    }
}
