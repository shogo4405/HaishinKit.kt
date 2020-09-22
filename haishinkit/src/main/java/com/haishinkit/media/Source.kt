package com.haishinkit.media

import com.haishinkit.lang.Running
import com.haishinkit.rtmp.RTMPStream

interface Source : Running {
    var stream: RTMPStream?

    fun setUp()
    fun tearDown()
}
