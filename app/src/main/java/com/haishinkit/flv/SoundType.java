package com.haishinkit.flv;

import com.haishinkit.lang.IRawValue;

public enum  SoundType implements IRawValue<Byte> {
    MONO((byte) 0x00),
    STEREO((byte) 0x01);

    private final Byte rawValue;

    SoundType(final byte rawValue) {
        this.rawValue = rawValue;
    }

    public final Byte rawValue() {
        return rawValue;
    }
}
