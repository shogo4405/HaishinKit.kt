package com.haishinkit.flv;

import com.haishinkit.lang.IRawValue;

public enum AudioCodec implements IRawValue<Byte> {
    PCM((byte) 0x00),
    ADPCM((byte) 0x01),
    MP3((byte) 0x02),
    PCMLE((byte) 0x03),
    NELLYMOSER16K((byte) 0x04),
    NELLYMOSER8K((byte) 0x05),
    NELLYMOSER((byte) 0x06),
    G711A((byte) 0x07),
    G711MU((byte) 0x08),
    AAC((byte) 0x0A),
    SPEEX((byte) 0x0B),
    MP3_8K((byte) 0x0E),
    UNKNOWN((byte) 0xFF);

    private final Byte rawValue;

    AudioCodec(final byte rawValue) {
        this.rawValue = rawValue;
    }

    public final Byte rawValue() {
        return rawValue;
    }
}
