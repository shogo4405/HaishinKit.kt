package com.haishinkit.flv;

import com.haishinkit.lang.IRawValue;

public enum SoundRate implements IRawValue<Byte> {
    kHz5_5((byte) 0x00),
    kHz11((byte) 0x01),
    kHz22((byte) 0x02),
    kHz44((byte) 0x03);

    private final Byte rawValue;

    SoundRate(final byte rawValue) {
        this.rawValue = rawValue;
    }

    public Byte rawValue() {
        return rawValue;
    }
}
