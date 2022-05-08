package com.haishinkit.view

import android.view.Surface
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.filter.VideoEffect
import com.haishinkit.net.NetStream

interface HkView {
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
     * Specifies the imageOrientation that describe the image orientation.
     */
    var imageOrientation: ImageOrientation

    /**
     * Attaches a video stream to the view.
     */
    fun attachStream(stream: NetStream?)

    fun createInputSurface(width: Int, height: Int, format: Int, lambda: ((surface: Surface) -> Unit))

    /**
     * Disposes the view for a memory management.
     */
    fun dispose()
}
