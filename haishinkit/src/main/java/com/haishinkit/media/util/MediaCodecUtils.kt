package com.haishinkit.media.util

import android.util.Log
import android.graphics.ImageFormat
import android.media.MediaCodecInfo
import android.media.MediaCodecList

object MediaCodecUtils {
    fun getCodecInfo(mimeType: String):MediaCodecInfo? {
        val numCodecs = MediaCodecList.getCodecCount()
        for (i in 0 until numCodecs) {
            val info = MediaCodecList.getCodecInfoAt(i)
            if (!info.isEncoder) {
                continue
            }
            val types = info.supportedTypes
            for (j in types.indices) {
                if (types[j].equals(mimeType, ignoreCase = true)) {
                    return info
                }
            }
        }
        return null
    }

    fun getColorFormat(codecInfo:MediaCodecInfo, mimeType:String):Int? {
        var capabilities = codecInfo.getCapabilitiesForType(mimeType)
        for (colorFormat in capabilities.colorFormats) {
            Log.i(javaClass.name, colorFormatToString(colorFormat))
        }
        for (colorFormat in capabilities.colorFormats) {
            if (isSupportedFormat(colorFormat)) {
                return colorFormat
            }
        }
        return null
    }

    fun imageFormatToString(imageFormat:Int):String {
        when (imageFormat) {
            ImageFormat.DEPTH16 -> return "DEPTH16"
            ImageFormat.DEPTH_POINT_CLOUD -> return "DEPTH_POINT_CLOUD"
            ImageFormat.FLEX_RGBA_8888 -> return "FLEX_RGBA_8888"
            ImageFormat.FLEX_RGB_888 -> return "FLEX_RGB_888"
            ImageFormat.JPEG -> return "JPEG"
            ImageFormat.NV16 -> return "NV16"
            ImageFormat.NV21 -> return "NV21"
            ImageFormat.PRIVATE -> return "PRIVATE"
            ImageFormat.RAW10 -> return "RAW10"
            ImageFormat.RAW12 -> return "RAW12"
            ImageFormat.RAW_PRIVATE -> return "RAW_PRIVATE"
            ImageFormat.RAW_SENSOR -> return "RAW_SENSOR"
            ImageFormat.RGB_565 -> return "RGB_565"
            ImageFormat.UNKNOWN -> return "UNKNOWN"
            ImageFormat.YUV_420_888 -> return "YUV_420_888"
            ImageFormat.YUV_422_888 -> return "YUV_422_888"
            ImageFormat.YUV_444_888 -> return "YUV_444_888"
            ImageFormat.YUY2 -> return "YUY2"
            ImageFormat.YV12 -> return "YV12"
            else -> return ""
        }
    }

    fun colorFormatToString(colorFormat:Int):String {
        when (colorFormat) {
            MediaCodecInfo.CodecCapabilities.COLOR_Format12bitRGB444 -> return "COLOR_Format12bitRGB444"
            MediaCodecInfo.CodecCapabilities.COLOR_Format16bitARGB1555 -> return "COLOR_Format16bitARGB1555"
            MediaCodecInfo.CodecCapabilities.COLOR_Format16bitARGB4444 -> return "COLOR_Format16bitARGB4444"
            MediaCodecInfo.CodecCapabilities.COLOR_Format16bitBGR565 -> return "COLOR_Format16bitBGR565"
            MediaCodecInfo.CodecCapabilities.COLOR_Format16bitRGB565 -> return "COLOR_Format16bitRGB565"
            MediaCodecInfo.CodecCapabilities.COLOR_Format18BitBGR666 -> return "COLOR_Format18BitBGR666"
            MediaCodecInfo.CodecCapabilities.COLOR_Format18bitARGB1665 -> return "COLOR_Format18bitARGB1665"
            MediaCodecInfo.CodecCapabilities.COLOR_Format18bitRGB666 -> return "COLOR_Format18bitRGB666"
            MediaCodecInfo.CodecCapabilities.COLOR_Format19bitARGB1666 -> return "COLOR_Format19bitARGB1666"
            MediaCodecInfo.CodecCapabilities.COLOR_Format24BitABGR6666 -> return "COLOR_Format24BitABGR6666"
            MediaCodecInfo.CodecCapabilities.COLOR_Format24BitARGB6666 -> return "COLOR_Format24BitARGB6666"
            MediaCodecInfo.CodecCapabilities.COLOR_Format24bitARGB1887 -> return "COLOR_Format24bitARGB1887"
            MediaCodecInfo.CodecCapabilities.COLOR_Format24bitBGR888 -> return "COLOR_Format24bitBGR888"
            MediaCodecInfo.CodecCapabilities.COLOR_Format24bitRGB888 -> return "COLOR_Format24bitRGB888"
            MediaCodecInfo.CodecCapabilities.COLOR_Format25bitARGB1888 -> return "COLOR_Format25bitARGB1888"
            MediaCodecInfo.CodecCapabilities.COLOR_Format32bitARGB8888 -> return "COLOR_Format32bitARGB8888"
            MediaCodecInfo.CodecCapabilities.COLOR_Format32bitBGRA8888 -> return "COLOR_Format32bitBGRA8888"
            MediaCodecInfo.CodecCapabilities.COLOR_Format8bitRGB332 -> return "COLOR_Format8bitRGB332"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatCbYCrY -> return "COLOR_FormatCbYCrY"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatCrYCbY -> return "COLOR_FormatCrYCbY"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatL16 -> return "COLOR_FormatL16"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatL2 -> return "COLOR_FormatL2"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatL24 -> return "COLOR_FormatL24"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatL32 -> return "COLOR_FormatL32"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatL4 -> return "COLOR_FormatL4"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatL8 -> return "COLOR_FormatL8"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatMonochrome -> return "COLOR_FormatMonochrome"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatRawBayer10bit -> return "COLOR_FormatRawBayer10bit"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatRawBayer8bit -> return "COLOR_FormatRawBayer8bit"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatRawBayer8bitcompressed -> return "COLOR_FormatRawBayer8bitcompressed"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYCbYCr -> return "COLOR_FormatYCbYCr"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYCrYCb -> return "COLOR_FormatYCrYCb"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV411PackedPlanar -> return "COLOR_FormatYUV411PackedPlanar"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV411Planar -> return "COLOR_FormatYUV411Planar"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar -> return "COLOR_FormatYUV420PackedPlanar"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar -> return "COLOR_FormatYUV420PackedSemiPlanar"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar -> return "COLOR_FormatYUV420Planar"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar -> return "COLOR_FormatYUV420SemiPlanar"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422PackedPlanar -> return "COLOR_FormatYUV422PackedPlanar"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422PackedSemiPlanar -> return "COLOR_FormatYUV422PackedSemiPlanar"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422Planar -> return "COLOR_FormatYUV422Planar"
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422SemiPlanar -> return "COLOR_FormatYUV422SemiPlanar";
            MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar -> return "COLOR_QCOM_FormatYUV420SemiPlanar"
            MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar -> return "COLOR_TI_FormatYUV420PackedSemiPlanar"
            else -> return ""
        }
    }

    private fun isSupportedFormat(colorFormat: Int):Boolean {
        return when (colorFormat) {
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar -> false
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar -> false
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar -> true
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar -> true
            MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar -> true
            else -> false
        }
    }
}
