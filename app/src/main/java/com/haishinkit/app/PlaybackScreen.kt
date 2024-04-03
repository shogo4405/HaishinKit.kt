package com.haishinkit.app

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.haishinkit.compose.HaishinKitView
import com.haishinkit.compose.rememberHaishinKitState
import com.haishinkit.rtmp.RtmpConnection

private const val TAG = "PlaybackScreen"

@Composable
fun PlaybackScreen(
    command: String,
    streamName: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    val haishinKitState = rememberHaishinKitState(
        context,
        onConnectionState = { state, data ->
            val code = data["code"].toString()
            Log.i(TAG, code)
            if (code == RtmpConnection.Code.CONNECT_SUCCESS.rawValue) {
                state.getStreamByName(streamName).play(streamName)
            }
        }) {
        RtmpConnection()
    }

    DisposableEffect(Unit) {
        onDispose {
            haishinKitState.dispose()
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        HaishinKitView(
            stream = haishinKitState.getStreamByName(streamName),
            modifier = Modifier.fillMaxSize()
        )
        Button(
            modifier = Modifier
                .padding(16.dp)
                .width(100.dp)
                .height(50.dp),
            onClick = {
                if (haishinKitState.isConnected) {
                    haishinKitState.close()
                } else {
                    haishinKitState.connect(command)
                }
            }
        ) {
            if (haishinKitState.isConnected) {
                Text("STOP")
            } else {
                Text("PLAY")
            }
        }
    }
}

@Preview
@Composable
private fun PreviewPlaybackScreen() {
    PlaybackScreen(command = "", streamName = "", modifier = Modifier.fillMaxSize())
}
