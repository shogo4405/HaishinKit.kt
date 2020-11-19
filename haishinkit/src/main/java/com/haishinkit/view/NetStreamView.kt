package com.haishinkit.view

import com.haishinkit.lang.Running
import com.haishinkit.rtmp.RtmpStream

internal interface NetStreamView : Running {
    var videoGravity: Int
    var stream: RtmpStream?

    fun attachStream(stream: RtmpStream?) {
        stream?.renderer = this
        this.stream = stream
        if (stream != null) {
            startRunning()
        } else {
            stopRunning()
        }
    }
}
