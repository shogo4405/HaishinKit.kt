package com.haishinkit.flv;

import com.haishinkit.lang.IRawValue;

public enum AACPacketType implements IRawValue<Byte> {
    SEQ((byte) 0x00),
    RAW((byte) 0x01);

    private final Byte rawValue;

    AACPacketType(final byte rawValue) {
        this.rawValue = rawValue;
    }

    public final Byte rawValue() {
        return rawValue;
    }
}
