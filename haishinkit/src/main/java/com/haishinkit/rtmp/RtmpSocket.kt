package com.haishinkit.rtmp

import android.util.Log
import com.haishinkit.event.Event
import com.haishinkit.net.NetSocket
import org.apache.commons.lang3.builder.ToStringBuilder
import java.lang.IllegalArgumentException
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer

internal class RtmpSocket(val connection: RtmpConnection) : NetSocket() {
    enum class ReadyState {
        Uninitialized,
        VersionSent,
        AckSent,
        HandshakeDone,
        Closing,
        Closed
    }

    var bandWidth = 0
        internal set
    var chunkSizeC = RtmpChunk.DEFAULT_SIZE
    var chunkSizeS = RtmpChunk.DEFAULT_SIZE
    var isConnected = false
        private set
    private val handshake: RtmpHandshake by lazy {
        RtmpHandshake()
    }
    private var readyState = ReadyState.Uninitialized
        set(value) {
            field = value
            connection.onSocketReadyStateChange(this, value)
        }

    override fun onTimeout() {
        close(false)
        connection.dispatchEventWith(Event.IO_ERROR, false)
        Log.i(TAG, "a connection was timeout")
    }

    override fun onConnect() {
        chunkSizeC = RtmpChunk.DEFAULT_SIZE
        chunkSizeS = RtmpChunk.DEFAULT_SIZE
        handshake.clear()
        readyState = ReadyState.VersionSent
        doOutput(handshake.c0C1Packet)
    }

    override fun close(disconnected: Boolean) {
        if (!isConnected) return
        var data: Any? = null
        if (disconnected) {
            data = if (readyState == ReadyState.HandshakeDone) {
                RtmpConnection.Code.CONNECT_CLOSED.data("")
            } else {
                RtmpConnection.Code.CONNECT_FAILED.data("")
            }
        }
        readyState = ReadyState.Closing
        super.close(disconnected)
        data?.let {
            connection.dispatchEventWith(Event.RTMP_STATUS, false, it)
        }
        readyState = ReadyState.Closed
        isConnected = false
    }

    override fun listen(buffer: ByteBuffer) {
        when (readyState) {
            ReadyState.VersionSent -> {
                if (buffer.limit() < RtmpHandshake.SIGNAL_SIZE + 1) {
                    return
                }
                handshake.s0S1Packet = buffer
                doOutput(handshake.c2Packet)
                buffer.position(RtmpHandshake.SIGNAL_SIZE + 1)
                readyState = ReadyState.AckSent
                if (buffer.limit() - buffer.position() == RtmpHandshake.SIGNAL_SIZE) {
                    listen(buffer.slice())
                    buffer.position(3073)
                }
            }
            ReadyState.AckSent -> {
                if (buffer.limit() < RtmpHandshake.SIGNAL_SIZE) {
                    return
                }
                handshake.s2Packet = buffer
                buffer.position(RtmpHandshake.SIGNAL_SIZE)
                readyState = ReadyState.HandshakeDone
                isConnected = true
                connection.uri?.let {
                    connection.doOutput(RtmpChunk.ZERO, connection.createConnectionMessage(it))
                }
            }
            ReadyState.HandshakeDone ->
                try {
                    connection.listen(buffer)
                } catch (e: IndexOutOfBoundsException) {
                    if (VERBOSE) Log.d(TAG, "", e)
                } catch (e: BufferUnderflowException) {
                    if (VERBOSE) Log.d(TAG, "", e)
                } catch (e: IllegalArgumentException) {
                    Log.w(TAG, "", e)
                    throw e
                }
            else -> {}
        }
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        private const val VERBOSE = false
        private var TAG = RtmpSocket::class.java.simpleName
    }
}
