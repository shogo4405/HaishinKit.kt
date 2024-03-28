package com.haishinkit.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpStream

@Composable
fun rememberConnectionState(): ConnectionState = remember { ConnectionState() }.apply {

}

class ConnectionState {
    internal val connection: RtmpConnection by lazy {
        RtmpConnection()
    }

    fun connect(command: String) {
        connection.connect(command)
    }

    internal fun createStream(context: Context): RtmpStream {
        return RtmpStream(context, connection)
    }
}
