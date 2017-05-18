package com.haishinkit.flv

enum class VideoCodec(val rawValue: Byte) {
    SORENSON_H263(0x02),
    SCREEN1(0x03),
    ON2_VP6(0x04),
    ON2_VP6_ALPHA(0x05),
    SCREEN2(0x06),
    AVC(0x07),
    UNKNOWN(Byte.MAX_VALUE);
}
