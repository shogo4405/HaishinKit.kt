package com.haishinkit.media

import com.haishinkit.data.VideoResolution

/**
 * An interface that captures a video source.
 */
interface VideoSource : Source {
    var resolution: VideoResolution
}
