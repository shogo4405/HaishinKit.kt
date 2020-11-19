package com.haishinkit.media

import com.haishinkit.lang.Running
import com.haishinkit.rtmp.RtmpStream

/**
 * An interface that captures a source.
 */
interface Source : Running {
    var stream: RtmpStream?

    fun setUp()
    fun tearDown()
}
