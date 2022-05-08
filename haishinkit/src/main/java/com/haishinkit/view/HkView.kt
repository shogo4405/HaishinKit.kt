package com.haishinkit.view

import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.lang.Running
import com.haishinkit.net.NetStream

interface HkView {
    var videoGravity: VideoGravity
    val pixelTransform: PixelTransform

    /**
     * Attaches a video stream to the view.
     */
    fun attachStream(stream: NetStream?)

    /**
     * Disposes the view for a memory management.
     */
    fun dispose() {
        pixelTransform.dispose()
    }
}
