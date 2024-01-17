package com.haishinkit.view

import android.graphics.Bitmap
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.net.NetStream

interface NetStreamDrawable {
    /**
     * Specifies the videoGravity how the displays the visual content.
     */
    var videoGravity: VideoGravity

    /**
     * Specifies the videoEffect such as a monochrome, a sepia.
     */
    var videoEffect: VideoEffect

    /**
     * Specifies the frameRate for an output source in frames/sec.
     */
    var frameRate: Int

    /**
     * Attaches a video stream to the view.
     */
    fun attachStream(stream: NetStream?)

    /**
     * Reads the pixels of a displayed image.
     */
    fun readPixels(lambda: ((bitmap: Bitmap?) -> Unit))
}
