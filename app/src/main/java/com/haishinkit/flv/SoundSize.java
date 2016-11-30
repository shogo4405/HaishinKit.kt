package com.haishinkit.flv;

import com.haishinkit.lang.IRawValue;

public enum SoundSize implements IRawValue<Byte> {
    SOUND_8BIT((byte) 0x00),
    SOUND_16BIT((byte) 0x01);

    private final Byte rawValue;

    SoundSize(final byte rawValue) {
        this.rawValue = rawValue;
    }

    public Byte rawValue() {
        return rawValue;
    }
}
