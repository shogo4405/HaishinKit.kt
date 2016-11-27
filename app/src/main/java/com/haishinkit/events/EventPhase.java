package com.haishinkit.events;

import com.haishinkit.lang.IRawValue;

public enum EventPhase implements IRawValue<Short> {
    NONE((short) 0x00),
    CAPTURING((short) 0x01),
    AT_TARGET((short) 0x02),
    BUBBLING((short) 0x03);

    private final short rawValue;

    EventPhase(final short rawValue) {
        this.rawValue = rawValue;
    }

    public Short rawValue() {
        return rawValue;
    }
}
