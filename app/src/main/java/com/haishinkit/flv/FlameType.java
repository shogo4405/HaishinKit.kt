package com.haishinkit.flv;

import com.haishinkit.lang.IRawValue;

public enum FlameType implements IRawValue<Byte> {
    KEY((byte) 0x01),
    INTER((byte) 0x02),
    DISPOSABLE((byte) 0x03),
    GENERATED((byte) 0x04),
    COMMAND((byte) 0x05);

    private final Byte rawValue;

    FlameType(final byte rawValue) {
        this.rawValue = rawValue;
    }

    public final Byte rawValue() {
        return rawValue;
    }
}
