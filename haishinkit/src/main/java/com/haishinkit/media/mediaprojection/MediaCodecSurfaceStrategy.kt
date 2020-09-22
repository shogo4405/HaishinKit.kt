package com.haishinkit.media.mediaprojection

import android.media.MediaCodecInfo
import android.util.DisplayMetrics
import android.view.Surface
import com.haishinkit.codec.MediaCodec
import com.haishinkit.rtmp.RTMPStream
import java.util.concurrent.atomic.AtomicBoolean

internal class MediaCodecSurfaceStrategy(override val metrics: DisplayMetrics) : SurfaceStrategy {
    private var _surface: Surface? = null
    override var surface: Surface?
        get() {
            if (_surface == null) {
                _surface = stream?.videoCodec?.createInputSurface()
            }
            return _surface
        }
        set(value) {
            _surface = value
        }
    override val isRunning = AtomicBoolean(false)
    override var stream: RTMPStream? = null

    override fun setUp() {
        stream?.videoCodec?.callback = MediaCodec.Callback(MediaCodec.MIME.VIDEO_AVC)
        stream?.videoCodec?.colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
    }

    override fun tearDown() {
        stream = null
        surface = null
    }

    override fun startRunning() {
        isRunning.set(true)
    }

    override fun stopRunning() {
        isRunning.set(false)
    }
}
