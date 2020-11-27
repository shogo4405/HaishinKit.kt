package com.haishinkit.flv

object FlvAudioCodec {
    const val ADPCM: Byte = 0x01
    const val MP3: Byte = 0x02
    const val PCMLE: Byte = 0x03
    const val NELLYMOSER16K: Byte = 0x04
    const val NELLYMOSER8K: Byte = 0x05
    const val NELLYMOSER: Byte = 0x06
    const val G711A: Byte = 0x07
    const val G711MU: Byte = 0x08
    const val AAC: Byte = 0x0A
    const val SPEEX: Byte = 0x0B
    const val MP3_8K: Byte = 0x0E
    const val UNKNOWN: Byte = Byte.MAX_VALUE
}
