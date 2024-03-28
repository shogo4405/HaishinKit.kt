package com.haishinkit.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.view.HkSurfaceView

@Composable
fun HaishinKitView(
    streamState: StreamState,
    modifier: Modifier = Modifier,
    videoGravity: VideoGravity = VideoGravity.RESIZE_ASPECT
) {
    AndroidView(
        factory = {
            HkSurfaceView(it).apply {
                this.videoGravity = videoGravity
                attachStream(streamState.stream)
            }
        },
        modifier = modifier
    )
}
