package com.haishinkit.net

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.CoroutineContext

internal abstract class Socket : CoroutineScope {
    var timeout = DEFAULT_TIMEOUT

    val totalBytesIn = AtomicLong(0)
    val totalBytesOut = AtomicLong(0)
    val queueBytesOut = AtomicLong(0)
    private var inputBuffer = ByteBuffer.allocate(0)
    private var socket: java.net.Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var outputQueue = ArrayBlockingQueue<ByteBuffer>(128)
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
    @Volatile private var keepAlive = false

    fun connect(dstName: String, dstPort: Int) {
        keepAlive = true
        launch(Dispatchers.IO) {
            doConnection(dstName, dstPort)
        }
    }

    open fun close(disconnected: Boolean) {
        keepAlive = false
        outputQueue.clear()
        IOUtils.closeQuietly(socket)
    }

    fun doOutput(buffer: ByteBuffer) {
        try {
            queueBytesOut.addAndGet(buffer.remaining().toLong())
            outputQueue.put(buffer)
        } catch (e: InterruptedException) {
            Log.v(javaClass.name + "#doOutput", "", e)
        }
    }

    protected abstract fun onTimeout()
    protected abstract fun onConnect()
    protected abstract fun listen(buffer: ByteBuffer)

    private fun doInput() {
        try {
            val available = inputStream?.available() ?: 0
            if (available == 0) {
                return
            }
            val buffer = ByteBuffer.allocate(inputBuffer.capacity() + available)
            buffer.put(inputBuffer)
            inputStream?.read(buffer.array(), inputBuffer.capacity(), available)
            buffer.position(0)
            totalBytesIn.addAndGet(buffer.remaining().toLong())
            listen(buffer)
            inputBuffer = buffer.slice()
        } catch (e: IOException) {
            Log.w(javaClass.name + "#doInput", "", e)
            close(true)
        }
    }

    private fun doOutput() {
        while (keepAlive) {
            for (buffer in outputQueue) {
                try {
                    val remaining = buffer.remaining().toLong()
                    buffer.flip()
                    outputStream?.write(buffer.array())
                    outputStream?.flush()
                    outputQueue.remove(buffer)
                    totalBytesOut.addAndGet(remaining)
                    queueBytesOut.addAndGet(remaining * -1)
                } catch (e: IOException) {
                    Log.w(javaClass.name + "#doOutput", "", e)
                    close(false)
                }
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
            }
        } catch (e: SocketTimeoutException) {
            Log.w(javaClass.name + "#doConnection", "", e)
            close(false)
            onTimeout()
        } catch (e: Exception) {
            Log.w(javaClass.name + "#doConnection", "", e)
            close(true)
        }
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val DEFAULT_TIMEOUT: Int = 1000
    }
}
