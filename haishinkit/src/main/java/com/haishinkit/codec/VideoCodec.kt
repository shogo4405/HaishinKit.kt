package com.haishinkit.codec

import android.content.res.AssetManager
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.os.Bundle
import android.util.Size
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.PixelTransformFactory
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.util.FeatureUtil
import kotlin.properties.Delegates

class VideoCodec : MediaCodec(MIME) {
    @Suppress("unused")
    data class Setting(private val codec: VideoCodec? = null) : MediaCodec.Setting(codec) {
        /**
         * The width resolution for video output.
         */
        var width: Int by Delegates.observable(DEFAULT_WIDTH) { _, oldValue, newValue ->
            if (oldValue != newValue) {
                codec?.width = newValue
            }
        }

        /**
         * The height resolution for video output.
         */
        var height: Int by Delegates.observable(DEFAULT_HEIGHT) { _, oldValue, newValue ->
            if (oldValue != newValue) {
                codec?.height = newValue
            }
        }

        /**
         * The bitrate for video output.
         */
        var bitRate: Int by Delegates.observable(DEFAULT_BIT_RATE) { _, oldValue, newValue ->
            if (oldValue != newValue) {
                codec?.bitRate = newValue
            }
        }

        /**
         * The IFrameInterval for video output.
         */
        var IFrameInterval: Int by Delegates.observable(DEFAULT_I_FRAME_INTERVAL) { _, oldValue, newValue ->
            if (oldValue != newValue) {
                codec?.IFrameInterval = newValue
            }
        }
        var frameRate: Int by Delegates.observable(DEFAULT_FRAME_RATE) { _, oldValue, newValue ->
            if (oldValue != newValue) {
                codec?.frameRate = newValue
            }
        }

        /**
         * The scaling mode for video output.
         */
        var videoGravity: VideoGravity by Delegates.observable(DEFAULT_VIDEO_GRAVITY) { _, oldValue, newValue ->
            if (oldValue != newValue) {
                codec?.videoGravity = newValue
            }
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
    var videoGravity: VideoGravity
        get() = pixelTransform.videoGravity
        set(value) {
            pixelTransform.videoGravity = value
        }

    val pixelTransform: PixelTransform by lazy {
        PixelTransformFactory().create().apply {
            videoGravity = videoGravity
        }
    }

    internal fun setListener(listener: PixelTransform.Listener?) {
        pixelTransform.listener = listener
    }

    internal fun setAssetManager(assetManager: AssetManager?) {
        pixelTransform.assetManager = assetManager
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
            pixelTransform.imageExtent = Size(width, height)
            pixelTransform.surface = codec.createInputSurface()
        }
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
        val DEFAULT_VIDEO_GRAVITY = VideoGravity.RESIZE_ASPECT

        private val TAG = VideoCodec::class.java.simpleName
    }
}
