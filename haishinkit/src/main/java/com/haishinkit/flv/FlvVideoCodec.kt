package com.haishinkit.flv

/**
 * The type of flv supports video codecs.
 */
@Suppress("unused")
object FlvVideoCodec {
    const val SORENSON_H263: Byte = 0x02
    const val SCREEN1: Byte = 0x03
    const val ON2_VP6: Byte = 0x04
    const val ON2_VP6_ALPHA: Byte = 0x05
    const val SCREEN_2: Byte = 0x06
    const val AVC: Byte = 0x07
    const val UNKNOWN: Byte = Byte.MAX_VALUE
}
