package com.haishinkit.media

import com.haishinkit.lang.IRunnable
import com.haishinkit.rtmp.RTMPStream

interface DeviceSource : IRunnable {
    var stream: RTMPStream?

    fun setUp()
    fun tearDown()
}
