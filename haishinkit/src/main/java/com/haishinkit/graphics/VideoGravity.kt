package com.haishinkit.graphics

/**
 * A value that specifies how the video is displayed within a layer’s bounds.
 */
enum class VideoGravity(val rawValue: Int) {
    RESIZE_ASPECT(0),
    RESIZE_ASPECT_FILL(1),
    RESIZE_ASPECT_RESIZE(2)
}
