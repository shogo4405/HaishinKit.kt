package com.haishinkit.media.codec

import java.io.IOException
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaCodecInfo
import com.haishinkit.yuv.NV21toYUV420SemiPlanarConverter
import com.haishinkit.media.util.MediaCodecUtils

internal class H264Encoder : EncoderBase(MIME) {
    var bitRate = DEFAULT_BIT_RATE
    var frameRate = DEFAULT_FRAME_RATE
    var IFrameInterval = DEFAULT_I_FRAME_INTERVAL
    var width = DEFAULT_WIDTH
    var height = DEFAULT_HEIGHT
    var profile = DEFAULT_PROFILE
    var level = DEFAULT_LEVEL

    override var byteConverter: ByteConverter? = NV21toYUV420SemiPlanarConverter()

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

        val converter: NV21toYUV420SemiPlanarConverter? = byteConverter as? NV21toYUV420SemiPlanarConverter
        converter?.width = width
        converter?.height = height

        return codec
    }

    companion object {
        val MIME = "video/avc"
        val DEFAULT_BIT_RATE = 125000
        val DEFAULT_FRAME_RATE = 15
        val DEFAULT_I_FRAME_INTERVAL = 2
        val DEFAULT_WIDTH = 1920
        val DEFAULT_HEIGHT = 1080
        val DEFAULT_PROFILE = MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline
        val DEFAULT_LEVEL = MediaCodecInfo.CodecProfileLevel.AVCLevel31
    }
}
