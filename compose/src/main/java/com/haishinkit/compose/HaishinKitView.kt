package com.haishinkit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.media.Stream
import com.haishinkit.view.HkSurfaceView
import com.haishinkit.view.HkTextureView

/**
 * The main view renders a [Stream] object.
 */
@Composable
fun HaishinKitView(
    streamState: StreamState,
    modifier: Modifier = Modifier,
    videoGravity: VideoGravity = VideoGravity.RESIZE_ASPECT,
    viewType: HaishinKitViewType = HaishinKitViewType.SurfaceView
) {
    val context = LocalContext.current

    val videoView = remember {
        when (viewType) {
            HaishinKitViewType.SurfaceView -> HkSurfaceView(context)
            HaishinKitViewType.TextureView -> HkTextureView(context)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            videoView.attachStream(null)
        }
    }

    AndroidView(
        factory = {
            videoView.apply {
                this.videoGravity = videoGravity
                attachStream(streamState.stream)
            }
        },
        modifier = modifier
    )
}
