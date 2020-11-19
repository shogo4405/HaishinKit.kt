package com.haishinkit.view

import com.haishinkit.lang.Running
import com.haishinkit.rtmp.RTMPStream

internal interface NetStreamView : Running {
    var videoGravity: Int
    var stream: RTMPStream?

    fun attachStream(stream: RTMPStream?) {
        stream?.renderer = this
        this.stream = stream
        if (stream != null) {
            startRunning()
        } else {
            stopRunning()
        }
    }
}
