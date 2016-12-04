package com.haishinkit.flv;

import com.haishinkit.lang.IRawValue;

public enum AVCPacketType implements IRawValue<Byte> {
    SEQ((byte) 0x00),
    NAL((byte) 0x01),
    EOS((byte) 0x02);

    private final Byte rawValue;

    AVCPacketType(final byte rawValue) {
        this.rawValue = rawValue;
    }

    public final Byte rawValue() {
        return rawValue;
    }
}
