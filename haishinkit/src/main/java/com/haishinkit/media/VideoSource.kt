package com.haishinkit.media

import android.util.Size

/**
 * An interface that captures a video source.
 */
interface VideoSource : Source {
    /**
     * The video size that is current capturing.
     */
    val resolution: Size
    val fpsControllerClass: Class<*>?
}
