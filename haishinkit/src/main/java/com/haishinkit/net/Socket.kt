package com.haishinkit.net

import android.util.Log
import androidx.core.util.Pools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.CoroutineContext
import kotlin.math.min

internal abstract class Socket : CoroutineScope {
    var timeout = DEFAULT_TIMEOUT

    val totalBytesIn = AtomicLong(0)
    val totalBytesOut = AtomicLong(0)
    val queueBytesOut = AtomicLong(0)
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
    private var inputBuffer = ByteBuffer.allocate(0)
    private var socket: java.net.Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var outputQueue = LinkedBlockingDeque<ByteBuffer>()
    private var outputBufferPool = Pools.SimplePool<ByteBuffer>(1024)
    @Volatile private var keepAlive = false

    fun connect(dstName: String, dstPort: Int) {
        keepAlive = true
        launch(coroutineContext) {
            doConnection(dstName, dstPort)
        }
    }

    open fun close(disconnected: Boolean) {
        keepAlive = false
        outputQueue.clear()
        socket?.close()
    }

    fun doOutput(buffer: ByteBuffer) {
        try {
            buffer.flip()
            queueBytesOut.addAndGet(buffer.remaining().toLong())
            outputQueue.put(buffer)
        } catch (e: InterruptedException) {
            Log.v(TAG, "", e)
        }
    }

    fun createByteBuffer(capacity: Int): ByteBuffer {
        synchronized(outputBufferPool) {
            var byteBuffer = outputBufferPool.acquire() ?: ByteBuffer.allocate(capacity)
            if (byteBuffer.capacity() != capacity) {
                byteBuffer = ByteBuffer.allocate(capacity)
            } else {
                byteBuffer.clear()
            }
            return byteBuffer
        }
    }

    protected abstract fun onTimeout()
    protected abstract fun onConnect()
    protected abstract fun listen(buffer: ByteBuffer)

    private fun doInput() {
        try {
            val inputStream = inputStream ?: return
            val available = inputStream.available()
            if (available == 0) {
                return
            }
            val capacity = inputBuffer.capacity()
            val buffer = ByteBuffer.allocate(capacity + available)
            buffer.put(inputBuffer)
            val result = inputStream.read(buffer.array(), capacity, available)
            val length = min(result, available)
            buffer.position(capacity + length)
            buffer.flip()
            totalBytesIn.addAndGet(buffer.remaining().toLong())
            listen(buffer)
            inputBuffer = buffer.slice()
        } catch (e: IOException) {
            Log.w(TAG, "", e)
            close(true)
        }
    }

    private fun doOutput() {
        while (keepAlive) {
            val buffer = outputQueue.take()
            try {
                val remaining = buffer.remaining().toLong()
                outputStream?.write(buffer.array(), 0, buffer.remaining())
                outputStream?.flush()
                outputQueue.remove(buffer)
                totalBytesOut.addAndGet(remaining)
                queueBytesOut.addAndGet(remaining * -1)
                synchronized(outputBufferPool) {
                    outputBufferPool.release(buffer)
                }
            } catch (e: IOException) {
                Log.w(TAG, "", e)
                close(false)
            }
        }
    }

    private fun doConnection(dstName: String, dstPort: Int) {
        try {
            outputQueue.clear()
            val endpoint = InetSocketAddress(dstName, dstPort)
            socket = java.net.Socket()
            socket?.connect(endpoint, timeout)
            if (socket!!.isConnected) {
                inputStream = socket?.getInputStream()
                outputStream = socket?.getOutputStream()
                launch(Dispatchers.IO) {
                    doOutput()
                }
                onConnect()
            }
            while (keepAlive) {
                doInput()
                try {
                    Thread.sleep(KEEP_ALIVE_SLEEP_INTERVAL)
                } catch (e: InterruptedException) {
                    Log.w(TAG, "", e)
                }
            }
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "", e)
            close(false)
            onTimeout()
        } catch (e: Exception) {
            Log.w(TAG, "", e)
            close(true)
        }
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val DEFAULT_TIMEOUT: Int = 1000

        private const val KEEP_ALIVE_SLEEP_INTERVAL = 100L
        private val TAG = Socket::class.java.simpleName
    }
}
