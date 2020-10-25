package com.haishinkit.event

enum class EventPhase(val rawValue: Short) {
    NONE(0x00),
    CAPTURING(0x01),
    AT_TARGET(0x02),
    BUBBLING(0x03);
}
