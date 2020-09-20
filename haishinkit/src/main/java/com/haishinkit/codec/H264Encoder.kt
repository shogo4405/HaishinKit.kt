package com.haishinkit.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import com.haishinkit.media.util.MediaCodecUtils
import com.haishinkit.yuv.NV21toYUV420SemiPlanarConverter
import java.io.IOException

internal class H264Encoder : EncoderBase(MIME) {
    var bitRate = DEFAULT_BIT_RATE
    var frameRate = DEFAULT_FRAME_RATE
    var IFrameInterval = DEFAULT_I_FRAME_INTERVAL
    var width = DEFAULT_WIDTH
    var height = DEFAULT_HEIGHT
    var profile = DEFAULT_PROFILE
    var level = DEFAULT_LEVEL

    override var byteConverter: ByteConverter = NV21toYUV420SemiPlanarConverter()

    @Throws(IOException::class)
    override fun createMediaCodec(): MediaCodec {
        val info = MediaCodecUtils.getCodecInfo(MIME)
        val colorFormat = MediaCodecUtils.getColorFormat(info!!, MIME)
        val mediaFormat = MediaFormat.createVideoFormat(MIME, width, height)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat!!)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFrameInterval)
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, profile)
        mediaFormat.setInteger(MediaFormat.KEY_AAC_ENCODED_TARGET_LEVEL, level)
        val codec = MediaCodec.createByCodecName(info.name)
        codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        return codec
    }

    companion object {
        const val MIME = "video/avc"
        const val DEFAULT_BIT_RATE = 125000
        const val DEFAULT_FRAME_RATE = 15
        const val DEFAULT_I_FRAME_INTERVAL = 2
        const val DEFAULT_WIDTH = 1920
        const val DEFAULT_HEIGHT = 1080
        const val DEFAULT_PROFILE = MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline
        const val DEFAULT_LEVEL = MediaCodecInfo.CodecProfileLevel.AVCLevel31
    }
}
