package com.haishinkit.rtmp.messages;

import com.haishinkit.flv.AudioCodec;
import com.haishinkit.flv.SoundRate;
import com.haishinkit.flv.SoundSize;
import com.haishinkit.flv.SoundType;
import com.haishinkit.iso.AudioSpecificConfig;
import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPSocket;

import java.nio.ByteBuffer;

public final class RTMPAACAudioMessage extends RTMPAudioMessage {
    private static final byte AAC = (byte)(0x0A << 4 | 0x03 << 2 | 0x01 << 1 | 0x01);
    private byte aacPacketType = 0;
    private AudioSpecificConfig config = null;

    public RTMPAACAudioMessage() {
        super();
        setCodec(AudioCodec.AAC.rawValue());
        setSoundRate(SoundRate.kHz44.rawValue());
        setSoundSize(SoundSize.SOUND_16BIT.rawValue());
        setSoundType(SoundType.STEREO.rawValue());
    }

    public final byte getAACPacketType() {
        return aacPacketType;
    }

    public final RTMPAACAudioMessage setAACPacketType(final byte aacPacketType) {
        this.aacPacketType = aacPacketType;
        return this;
    }

    public final AudioSpecificConfig getConfig() {
        return config;
    }

    public final RTMPAACAudioMessage setConfig(final AudioSpecificConfig config) {
        this.config = config;
        return this;
    }

    @Override
    public ByteBuffer encode(final RTMPSocket socket) {
        if (socket == null) {
            throw new IllegalArgumentException();
        }

        ByteBuffer buffer = null;
        final int length = getPayload() == null ? 0 : getPayload().limit();
        switch (aacPacketType) {
            case 0x00:
                buffer = ByteBuffer.allocate(2 + length);
                buffer.put(AAC);
                buffer.put(getAACPacketType());
                if (0 < length) {
                    buffer.put(getPayload());
                }
                break;
            case 0x01:
                buffer = ByteBuffer.allocate(2 + AudioSpecificConfig.ADTS_HEADER_SIZE + length);
                buffer.put(AAC);
                buffer.put(getAACPacketType());
                if (0 < length) {
                    buffer.put(config.toADTS(length));
                    buffer.put(getPayload());
                }
                break;
        }

        return buffer;
    }

    @Override
    public RTMPMessage decode(final ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        buffer.position(buffer.position() + getLength());
        return this;
    }

    @Override
    public RTMPMessage execute(final RTMPConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException();
        }
        return this;
    }
}
