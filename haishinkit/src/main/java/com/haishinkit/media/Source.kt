package com.haishinkit.media

import com.haishinkit.lang.Running

/**
 * An interface that captures a source.
 */
interface Source : Running {
    var stream: Stream?
}
