package com.haishinkit.net

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

interface NetSocket {
    interface Listener {
        fun onInput(buffer: ByteBuffer)
        fun onTimeout()
        fun onConnect()
    }

    var timeout: Int
    var listener: Listener?
    val totalBytesIn: AtomicLong
    val totalBytesOut: AtomicLong
    val queueBytesOut: AtomicLong

    fun connect(dstName: String, dstPort: Int, isSecure: Boolean)
    fun doOutput(buffer: ByteBuffer)
    fun close(disconnected: Boolean)
    fun createByteBuffer(capacity: Int): ByteBuffer
}
