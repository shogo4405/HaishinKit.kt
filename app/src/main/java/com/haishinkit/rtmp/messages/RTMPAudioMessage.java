package com.haishinkit.rtmp.messages;

import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPSocket;

import java.nio.ByteBuffer;

public final class RTMPAudioMessage extends RTMPMessage {
    private byte codec = 0;
    private byte soundRate = 0;
    private byte soundSize = 0;
    private byte soundType = 0;
    private ByteBuffer payload = null;

    public RTMPAudioMessage() {
        super(Type.AUDIO);
    }

    public byte getCodec() {
        return codec;
    }

    public RTMPAudioMessage setCodec(final byte codec) {
        this.codec = codec;
        return this;
    }

    public byte getSoundRate() {
        return soundRate;
    }

    public RTMPAudioMessage setSoundRate(final byte soundRate) {
        this.soundRate = soundRate;
        return this;
    }

    public byte getSoundSize() {
        return soundSize;
    }

    public RTMPAudioMessage setSoundSize(final byte soundSize) {
        this.soundSize = soundSize;
        return this;
    }

    public byte getSoundType() {
        return soundType;
    }

    public RTMPAudioMessage setSoundType(final byte soundType) {
        this.soundType = soundType;
        return this;
    }

    public ByteBuffer getPayload() {
        return payload;
    }

    public RTMPAudioMessage setPayload(final ByteBuffer payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public RTMPMessage decode(final ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        byte first = buffer.get();
        setCodec((byte)(first >> 4));
        setSoundRate((byte)(first & 0b00001100 >> 2));
        setSoundSize((byte)(first & 0b00000010 >> 1));
        setSoundType((byte)(first & 0b00000001));
        byte[] payload = new byte[getLength() - 1];
        buffer.get(payload);
        setPayload(ByteBuffer.wrap(payload));
        return this;
    }

    @Override
    public RTMPMessage execute(final RTMPConnection connection) {
        return this;
    }
}
