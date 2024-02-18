package com.haishinkit.media

import com.haishinkit.screen.ScreenObjectContainer

/**
 * An interface that captures a video source.
 */
interface VideoSource : Source {
    /**
     * The video screen container object.
     */
    val screen: ScreenObjectContainer
}
