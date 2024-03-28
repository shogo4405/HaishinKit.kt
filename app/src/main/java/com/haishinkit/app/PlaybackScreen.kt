package com.haishinkit.app

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.haishinkit.compose.HaishinKitView
import com.haishinkit.compose.rememberConnectionState
import com.haishinkit.compose.rememberStreamState
import com.haishinkit.rtmp.RtmpConnection

@Composable
fun PlaybackScreen(
    command: String,
    streamName: String,
    modifier: Modifier
) {
    val connectionState = rememberConnectionState()
    val streamState = rememberStreamState(
        context = LocalContext.current,
        connectionState = connectionState,
        connectionStateChange = { stream, data ->
            val code = data["code"].toString()
            if (code == RtmpConnection.Code.CONNECT_SUCCESS.rawValue) {
                stream.play(streamName)
            }
        }
    )
    Box(modifier = modifier) {
        HaishinKitView(
            streamState = streamState,
            modifier = Modifier.fillMaxSize()
        )
        Button(
            modifier = Modifier
                .width(100.dp)
                .height(50.dp),
            onClick = {
                Log.w(TAG, "GO LIVE!!")
                connectionState.connect(command)
            }) {
            Text("GO LIVE")
        }
    }
}

private const val TAG = "PlaybackScreen"
