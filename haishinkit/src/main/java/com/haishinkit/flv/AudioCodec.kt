package com.haishinkit.flv

enum class AudioCodec(val rawValue: Byte) {
    ADPCM(0x01),
    MP3(0x02),
    PCMLE(0x03),
    NELLYMOSER16K(0x04),
    NELLYMOSER8K(0x05),
    NELLYMOSER(0x06),
    G711A(0x07),
    G711MU(0x08),
    AAC(0x0A),
    SPEEX(0x0B),
    MP3_8K(0x0E),
    UNKNOWN(Byte.MAX_VALUE);
}
