package com.haishinkit.media.mediaprojection

import android.media.MediaCodecInfo
import android.util.DisplayMetrics
import android.view.Surface
import com.haishinkit.rtmp.RTMPStream

internal class MediaCodecSurfaceStrategy(override val metrics: DisplayMetrics) : SurfaceStrategy {
    override var surface: Surface? = null
    override val isRunning: Boolean = false
    override var stream: RTMPStream? = null

    override fun setUp() {
        stream?.videoCodec?.colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        surface = stream?.videoCodec?.createInputSurface()
    }

    override fun tearDown() {
    }

    override fun startRunning() {
    }

    override fun stopRunning() {
    }
}
