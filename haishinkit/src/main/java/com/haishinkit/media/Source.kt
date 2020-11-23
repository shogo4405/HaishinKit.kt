package com.haishinkit.media

import com.haishinkit.lang.Running
import com.haishinkit.lang.Utilize
import com.haishinkit.net.NetStream

/**
 * An interface that captures a source.
 */
interface Source : Running, Utilize {
    var stream: NetStream?
}
