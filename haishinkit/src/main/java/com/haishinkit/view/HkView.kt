package com.haishinkit.view

import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.lang.Running
import com.haishinkit.net.NetStream

interface HkView : Running {
    var videoGravity: VideoGravity
    var stream: NetStream?
    val pixelTransform: PixelTransform

    /**
     * Attaches a video stream to the view.
     */
    fun attachStream(stream: NetStream?) {
        stream?.renderer = this
        this.stream = stream
        if (stream != null) {
            startRunning()
        } else {
            stopRunning()
        }
    }

    /**
     * Disposes the view for a memory management.
     */
    fun dispose() {
        pixelTransform.dispose()
    }
}
