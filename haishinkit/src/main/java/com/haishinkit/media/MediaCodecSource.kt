package com.haishinkit.media

import android.graphics.Rect
import android.util.Size
import android.view.Surface
import com.haishinkit.screen.ScreenObjectContainer
import com.haishinkit.screen.Video
import java.util.concurrent.atomic.AtomicBoolean

internal class MediaCodecSource(val size: Size) : VideoSource, Video.OnSurfaceChangedListener {
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    override var stream: Stream? = null
    override val screen: ScreenObjectContainer by lazy {
        ScreenObjectContainer()
    }

    private val video: Video by lazy {
        Video().apply {
            isRotatesWithContent = false
        }
    }

    override fun startRunning() {
        if (isRunning.get()) return
        isRunning.set(true)
        stream?.screen?.frame = Rect(0, 0, size.width, size.height)
        video.videoSize = size
        video.listener = this
        screen.addChild(video)
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        video.listener = null
        screen.removeChild(video)
        isRunning.set(false)
    }

    override fun onSurfaceChanged(surface: Surface?) {
        stream?.videoCodec?.surface = surface
    }
}
