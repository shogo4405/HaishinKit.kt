package com.haishinkit.media

import com.haishinkit.lang.Running
import com.haishinkit.rtmp.RTMPStream

/**
 * An interface that captures a source.
 */
interface Source : Running {
    var stream: RTMPStream?

    fun setUp()
    fun tearDown()
}
