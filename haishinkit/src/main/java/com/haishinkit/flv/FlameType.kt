package com.haishinkit.flv

enum class FlameType(val rawValue: Byte) {
    KEY(0x01),
    INTER(0x02),
    DISPOSABLE(0x03),
    GENERATED(0x04),
    COMMAND(0x05);
}
