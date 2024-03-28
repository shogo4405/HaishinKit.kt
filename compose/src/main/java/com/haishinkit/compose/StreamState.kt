package com.haishinkit.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.haishinkit.event.Event
import com.haishinkit.event.EventUtils
import com.haishinkit.event.IEventListener
import com.haishinkit.rtmp.RtmpStream

@Composable
fun rememberStreamState(
    connectionState: ConnectionState,
    connectionStateChange: (stream: RtmpStream, data: Map<String, Any>) -> Unit,
    context: Context,
): StreamState = remember {
    StreamStateImpl(
        connectionState.createStream(context),
        connectionStateChange
    )
}.apply {
    connectionState.connection.addEventListener(Event.RTMP_STATUS, this)
}

interface StreamState {
    val receiveAudio: Boolean
    val receiveVideo: Boolean
    val stream: RtmpStream

    fun play(name: String)
}

internal class StreamStateImpl(
    override val stream: RtmpStream,
    val connectionStateChange: (stream: RtmpStream, data: Map<String, Any>) -> Unit
) : StreamState, IEventListener {
    override val receiveAudio: Boolean by mutableStateOf(stream.receiveAudio)
    override val receiveVideo: Boolean by mutableStateOf(stream.receiveVideo)

    override fun play(name: String) {
        stream.play(name)
    }

    override fun handleEvent(event: Event) {
        val data = EventUtils.toMap(event)
        connectionStateChange(stream, data)
    }
}
