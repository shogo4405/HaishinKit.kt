package com.haishinkit.flv;

import com.haishinkit.lang.IRawValue;

public enum VideoCodec implements IRawValue<Byte> {
    SORENSON_H263((byte) 0x02),
    SCREEN1((byte) 0x03),
    ON2_VP6((byte) 0x04),
    ON2_VP6_ALPHA((byte) 0x05),
    SCREEN2((byte) 0x06),
    AVC((byte) 0x07),
    UNKNOWN((byte) 0xFF);

    private final byte rawValue;

    VideoCodec(final byte rawValue) {
        this.rawValue = rawValue;
    }

    public final Byte rawValue() {
        return this.rawValue;
    }
}
