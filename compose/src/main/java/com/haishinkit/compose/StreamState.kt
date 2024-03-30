package com.haishinkit.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.haishinkit.event.Event
import com.haishinkit.event.EventUtils
import com.haishinkit.event.IEventListener
import com.haishinkit.rtmp.RtmpStream

/*
* Create and [remember] a [StreamState] instance.
*/
@Composable
fun rememberStreamState(
    connectionState: ConnectionState,
    connectionStateChange: (stream: RtmpStream, data: Map<String, Any>) -> Unit,
    context: Context,
): StreamState = remember {
    StreamState(
        connectionState.createStream(context),
        connectionStateChange
    )
}.apply {
    connectionState.connection.addEventListener(Event.RTMP_STATUS, listener)
}

@Suppress("UNUSED")
@Stable
class StreamState(
    val stream: RtmpStream,
    val connectionStateChange: (stream: RtmpStream, data: Map<String, Any>) -> Unit
) {
    internal val listener = object : IEventListener {
        override fun handleEvent(event: Event) {
            val data = EventUtils.toMap(event)
            connectionStateChange(stream, data)
        }
    }

    /**
     * @see [RtmpStream.play]
     */
    fun play(name: String) {
        stream.play(name)
    }

    /**
     * @see [RtmpStream.dispose]
     */
    fun dispose() {
        stream.dispose()
    }
}
