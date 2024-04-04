@file:Suppress("MemberVisibilityCanBePrivate")

package com.haishinkit.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.haishinkit.event.Event
import com.haishinkit.event.EventUtils
import com.haishinkit.event.IEventListener
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpStream

/*
* Create and [remember] a [ConnectionState] instance.
*/
@Composable
fun rememberConnectionState(
    factory: () -> RtmpConnection,
): ConnectionState =
    remember {
        ConnectionState(
            factory(),
        )
    }

@Suppress("UNUSED")
@Stable
class ConnectionState(
    private val connection: RtmpConnection,
) {
    var code: String by mutableStateOf(RtmpConnection.Code.CONNECT_CLOSED.rawValue)
        private set
    var isConnected by mutableStateOf(connection.isConnected)
        private set

    private val listener =
        object : IEventListener {
            override fun handleEvent(event: Event) {
                val data = EventUtils.toMap(event)
                code = data["code"].toString()
                isConnected = connection.isConnected
            }
        }

    init {
        connection.addEventListener(Event.RTMP_STATUS, listener)
    }

    fun connect(
        command: String,
        vararg arguments: Any?,
    ) {
        connection.connect(command = command, arguments = arguments)
    }

    fun close() {
        connection.close()
        isConnected = false
    }

    fun createStream(context: Context): RtmpStream {
        return RtmpStream(context, connection)
    }

    fun dispose() {
        connection.removeEventListener(Event.RTMP_STATUS, listener)
        connection.dispose()
    }
}
