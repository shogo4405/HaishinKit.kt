package com.haishinkit.codec

import android.graphics.SurfaceTexture
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import com.haishinkit.flv.tag.FlvTag
import com.haishinkit.gles.GlPixelContext
import com.haishinkit.gles.GlPixelReader
import com.haishinkit.gles.GlPixelTransform
import com.haishinkit.util.FeatureUtil
import java.nio.ByteBuffer
import kotlin.properties.Delegates

class VideoCodec : MediaCodec(MIME), GlPixelReader.Listener {
    class Setting(var codec: VideoCodec? = null) : MediaCodec.Setting(codec) {
        var width: Int by Delegates.observable(DEFAULT_WIDTH) { _, _, newValue ->
            codec?.width = newValue
        }
        var height: Int by Delegates.observable(DEFAULT_HEIGHT) { _, _, newValue ->
            codec?.height = newValue
        }
        var bitRate: Int by Delegates.observable(DEFAULT_BIT_RATE) { _, _, newValue ->
            codec?.bitRate = newValue
        }
        var IFrameInterval: Int by Delegates.observable(DEFAULT_I_FRAME_INTERVAL) { _, _, newValue ->
            codec?.IFrameInterval = newValue
        }
        var frameRate: Int by Delegates.observable(DEFAULT_FRAME_RATE) { _, _, newValue ->
            codec?.frameRate = newValue
        }
        var pixelRendererClass: Class<*>? by Delegates.observable(null) { _, _, newValue ->
            codec?.pixelRendererClass = newValue
        }
    }

    var bitRate = DEFAULT_BIT_RATE
        set(value) {
            field = value
            if (FeatureUtil.has(FeatureUtil.FEATURE_BITRATE_CHANGE) && isRunning.get()) {
                val bundle = Bundle().apply {
                    putInt(android.media.MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, value)
                }
                codec?.setParameters(bundle)
            }
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
    var pixelRendererClass: Class<*>? = null

    private val pixelTransform: GlPixelTransform by lazy {
        val transform = GlPixelTransform()
        transform.reader.listener = this
        transform
    }

    internal fun setListener(listener: GlPixelTransform.Listener?) {
        pixelTransform.setListener(listener)
    }

    fun frameAvailable(surfaceTexture: SurfaceTexture) {
        if (!isRunning.get() || mode == MODE_DECODE) return
        pixelTransform.frameAvailable(surfaceTexture)
    }

    override fun createOutputFormat(): MediaFormat {
        return MediaFormat.createVideoFormat(MIME, width, height).apply {
            if (mode == MODE_ENCODE) {
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_CAPTURE_RATE, frameRate)
                setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / frameRate)
                setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFrameInterval)
                setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
            } else {
                if (Build.VERSION_CODES.R <= Build.VERSION.SDK_INT) {
                    setInteger(MediaFormat.KEY_LOW_LATENCY, 1)
                }
            }
            setInteger(MediaFormat.KEY_PROFILE, profile)
            if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
                setInteger(MediaFormat.KEY_LEVEL, level)
            } else {
                setInteger("level", level)
            }
        }
    }

    override fun configure(codec: android.media.MediaCodec) {
        super.configure(codec)
        if (mode == MODE_ENCODE) {
            pixelTransform.fpsControllerClass = fpsControllerClass
            pixelTransform.pixelRendererClass = pixelRendererClass
            pixelTransform.configure(codec.createInputSurface(), width, height)
        }
    }

    override fun execute(buffer: ByteBuffer, timestamp: Long) {
        listener?.onCaptureOutput(FlvTag.TYPE_VIDEO, buffer, timestamp)
    }

    companion object {
        const val MIME = MIME_VIDEO_AVC

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
