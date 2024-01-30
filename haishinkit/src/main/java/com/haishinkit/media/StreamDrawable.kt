package com.haishinkit.media

import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.effect.VideoEffect

interface StreamDrawable {
    /**
     * Specifies the videoGravity how the displays the visual content.
     */
    var videoGravity: VideoGravity

    /**
     * Specifies the videoEffect such as a monochrome, a sepia.
     */
    var videoEffect: VideoEffect

    /**
     * Specifies the frameRate for an output source in frames/sec.
     */
    var frameRate: Int

    /**
     * Attaches a video stream to the view.
     */
    fun attachStream(stream: Stream?)
}
