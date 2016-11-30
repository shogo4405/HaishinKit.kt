package com.haishinkit.flv;

import com.haishinkit.lang.IRawValue;

public enum AVCPacketType implements IRawValue<Byte> {
    SEQ((byte) 0x01),
    NAL((byte) 0x02),
    EOS((byte) 0x03);

    private final Byte rawValue;

    AVCPacketType(final byte rawValue) {
        this.rawValue = rawValue;
    }

    public Byte rawValue() {
        return rawValue;
    }
}
