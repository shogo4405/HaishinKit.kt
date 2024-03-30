package com.haishinkit.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.haishinkit.event.Event
import com.haishinkit.event.IEventListener
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpStream

/*
* Create and [remember] a [ConnectionState] instance.
*/
@Composable
fun rememberConnectionState(): ConnectionState = remember { ConnectionState(RtmpConnection()) }

@Suppress("UNUSED")
@Stable
class ConnectionState(val connection: RtmpConnection) {
    var isConnected by mutableStateOf(connection.isConnected)
        private set

    private val listener = object : IEventListener {
        override fun handleEvent(event: Event) {
            isConnected = connection.isConnected
        }
    }

    init {
        connection.addEventListener(Event.RTMP_STATUS, listener)
    }

    /**
     * @see [RtmpConnection.connect]]
     */
    fun connect(command: String) {
        connection.connect(command)
    }

    /**
     * @see [RtmpConnection.close]
     */
    fun close() {
        connection.close()
        isConnected = connection.isConnected
    }

    /**
     * @see [RtmpConnection.dispose]
     */
    fun dispose() {
        connection.removeEventListener(Event.RTMP_STATUS, listener)
        connection.dispose()
    }

    internal fun createStream(context: Context): RtmpStream {
        return RtmpStream(context, connection)
    }
}
