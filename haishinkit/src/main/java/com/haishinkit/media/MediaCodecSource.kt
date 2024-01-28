package com.haishinkit.media

import android.graphics.Point
import android.util.Size
import android.view.Surface
import com.haishinkit.screen.Video
import com.haishinkit.util.Rectangle
import java.util.concurrent.atomic.AtomicBoolean

internal class MediaCodecSource(val size: Size) : VideoSource, Video.OnSurfaceChangedListener {
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    override var stream: Stream? = null
    override val screen: Video by lazy {
        Video().apply {
            isRotatesWithContent = false
        }
    }

    override fun startRunning() {
        if (isRunning.get()) return
        isRunning.set(true)
        stream?.screen?.frame = Rectangle(Point(0, 0), size)
        screen.videoSize = size
        screen.listener = this
        stream?.screen?.addChild(screen)
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        stream?.screen?.removeChild(screen)
        screen.listener = null
        isRunning.set(false)
    }

    override fun onSurfaceChanged(surface: Surface?) {
        stream?.videoCodec?.surface = surface
    }
}
