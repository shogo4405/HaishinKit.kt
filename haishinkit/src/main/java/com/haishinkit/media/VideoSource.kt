package com.haishinkit.media

import android.util.Size

/**
 * An interface that captures a video source.
 */
interface VideoSource : Source {
    var resolution: Size
    val fpsControllerClass: Class<*>?
}
