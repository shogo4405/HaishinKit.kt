package com.haishinkit.media

import com.haishinkit.screen.Video

/**
 * An interface that captures a video source.
 */
interface VideoSource : Source {
    /**
     * The video screen object.
     */
    val screen: Video
}
