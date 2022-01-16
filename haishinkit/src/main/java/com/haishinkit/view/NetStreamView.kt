package com.haishinkit.view

import com.haishinkit.lang.Running
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.net.NetStream

internal interface NetStreamView : Running {
    var videoGravity: Int
    var stream: NetStream?
    val pixelTransform: PixelTransform

    fun attachStream(stream: NetStream?) {
        stream?.renderer = this
        this.stream = stream
        if (stream != null) {
            startRunning()
        } else {
            stopRunning()
        }
    }
}
