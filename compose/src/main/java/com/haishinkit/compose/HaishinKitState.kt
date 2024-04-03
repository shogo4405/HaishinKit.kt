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

typealias OnConnectionState = (state: HaishinKitState, data: Map<String, Any>) -> Unit

/*
* Create and [remember] a [HaishinKitState] instance.
*/
@Composable
fun rememberHaishinKitState(
    context: Context,
    onConnectionState: OnConnectionState,
    factory: () -> RtmpConnection,
): HaishinKitState =
    remember(context) {
        HaishinKitState(
            onConnectionState,
            factory(),
            context,
        )
    }

@Suppress("UNUSED")
@Stable
class HaishinKitState(
    onConnectionState: OnConnectionState,
    private val connection: RtmpConnection,
    private val context: Context,
) {
    var isConnected by mutableStateOf(connection.isConnected)
        private set

    private val streams = mutableMapOf<String, RtmpStream>()

    private val listener =
        object : IEventListener {
            override fun handleEvent(event: Event) {
                val data = EventUtils.toMap(event)
                isConnected = connection.isConnected
                onConnectionState.invoke(this@HaishinKitState, data)
            }
        }

    init {
        connection.addEventListener(Event.RTMP_STATUS, listener)
    }

    fun getStreamByName(name: String): RtmpStream {
        var stream = streams[name]
        if (stream != null) {
            return stream
        }
        stream = RtmpStream(context, connection)
        streams[name] = stream
        return stream
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

    fun dispose() {
        connection.removeEventListener(Event.RTMP_STATUS, listener)
        connection.dispose()
    }
}
