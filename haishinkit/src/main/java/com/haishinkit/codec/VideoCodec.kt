package com.haishinkit.codec

import android.content.res.AssetManager
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.util.Size
import com.haishinkit.flv.tag.FlvTag
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.PixelTransformFactory
import com.haishinkit.graphics.gles.GlPixelReader
import com.haishinkit.util.FeatureUtil
import java.nio.ByteBuffer
import kotlin.properties.Delegates

class VideoCodec : MediaCodec(MIME), GlPixelReader.Listener {
    @Suppress("unused")
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
    }

    var bitRate = DEFAULT_BIT_RATE
        set(value) {
            field = value
            if (FeatureUtil.isEnabled(FeatureUtil.FEATURE_BITRATE_CHANGE) && isRunning.get()) {
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
    var fpsControllerClass: Class<*>? = null

    val pixelTransform: PixelTransform by lazy {
        PixelTransformFactory().create()
    }

    internal fun setListener(listener: PixelTransform.Listener?) {
        pixelTransform.listener = listener
    }

    internal fun setAssetManager(assetManager: AssetManager?) {
        pixelTransform.assetManager = assetManager
    }

    internal fun createInputSurface(width: Int, height: Int, format: Int) {
        pixelTransform.createInputSurface(width, height, format)
    }

    override fun createOutputFormat(): MediaFormat {
        return MediaFormat.createVideoFormat(MIME, width, height).apply {
            if (mode == Mode.ENCODE) {
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
        if (mode == Mode.ENCODE) {
            pixelTransform.fpsControllerClass = fpsControllerClass
            pixelTransform.extent = Size(width, height)
            pixelTransform.surface = codec.createInputSurface()
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
