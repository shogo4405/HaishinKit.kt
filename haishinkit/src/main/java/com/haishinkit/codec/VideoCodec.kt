package com.haishinkit.codec

import android.graphics.SurfaceTexture
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import com.haishinkit.gles.GlPixelContext
import com.haishinkit.gles.GlPixelTransform

internal class VideoCodec() : MediaCodec(MIME) {
    var bitRate = DEFAULT_BIT_RATE
        set(value) {
            field = value
            val bundle = Bundle().apply {
                putInt(android.media.MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, value)
            }
            codec?.setParameters(bundle)
        }
    var frameRate = DEFAULT_FRAME_RATE
    var IFrameInterval = DEFAULT_I_FRAME_INTERVAL
    var width = DEFAULT_WIDTH
    var height = DEFAULT_HEIGHT
    var profile = DEFAULT_PROFILE
    var level = DEFAULT_LEVEL
    var colorFormat = DEFAULT_COLOR_FORMAT
    var context: GlPixelContext
        get() = pixelTransform.context
        set(value) {
            pixelTransform.context = value
        }
    var fpsControllerClass: Class<*>? = null
    private var pixelTransform: GlPixelTransform = GlPixelTransform()

    fun setListener(listener: GlPixelTransform.Listener?) {
        pixelTransform.setListener(listener)
    }

    fun frameAvailable(surfaceTexture: SurfaceTexture) {
        if (!isRunning.get()) return
        pixelTransform.frameAvailable(surfaceTexture)
    }

    override fun createOutputFormat(): MediaFormat {
        return MediaFormat.createVideoFormat(MIME, width, height).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_CAPTURE_RATE, frameRate)
            setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / frameRate)
            setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFrameInterval)
            setInteger(MediaFormat.KEY_PROFILE, profile)
            if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
                setInteger(MediaFormat.KEY_LEVEL, level)
            } else {
                setInteger("level", level)
            }
            setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
        }
    }

    override fun configure(codec: android.media.MediaCodec) {
        super.configure(codec)
        pixelTransform.fpsControllerClass = fpsControllerClass
        pixelTransform.configure(codec.createInputSurface(), width, height)
    }

    companion object {
        const val MIME = com.haishinkit.codec.MediaCodec.MIME_VIDEO_AVC

        const val DEFAULT_BIT_RATE = 500 * 1000
        const val DEFAULT_FRAME_RATE = 30
        const val DEFAULT_I_FRAME_INTERVAL = 2
        const val DEFAULT_WIDTH = 640
        const val DEFAULT_HEIGHT = 360
        const val DEFAULT_PROFILE = MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline
        const val DEFAULT_LEVEL = MediaCodecInfo.CodecProfileLevel.AVCLevel31
        const val DEFAULT_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface

        private val TAG = VideoCodec::class.java.simpleName
    }
}
