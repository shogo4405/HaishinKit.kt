package com.haishinkit.media

import com.haishinkit.lang.Running
import com.haishinkit.net.NetStream

/**
 * An interface that captures a source.
 */
interface Source : Running {
    var stream: NetStream?

    fun setUp()
    fun tearDown()
}
