package com.haishinkit.flv

enum class AVCPacketType(val rawValue: Byte) {
    SEQ(0x00),
    NAL(0x01),
    EOS(0x02);
}
