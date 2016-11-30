package com.haishinkit.rtmp.messages;

import com.haishinkit.flv.VideoCodec;
import com.haishinkit.rtmp.RTMPSocket;

import java.nio.ByteBuffer;

public final class RTMPAVCVideoMessage extends RTMPVideoMessage {
    private byte packetType = 0;
    private int compositeTime = 0;

    public RTMPAVCVideoMessage() {
        super();
        setCodec(VideoCodec.AVC.rawValue());
    }

    public byte getPacketType() {
        return packetType;
    }

    public RTMPAVCVideoMessage setPacketType(final byte packetType) {
        this.packetType = packetType;
        return this;
    }

    public int getCompositeTime() {
        return compositeTime;
    }

    public RTMPAVCVideoMessage setCompositeTime(final byte compositeTime) {
        this.compositeTime = compositeTime;
        return this;
    }

    @Override
    public ByteBuffer encode(final RTMPSocket socket) {
        if (socket == null) {
            throw new IllegalArgumentException();
        }
        int length = getPayload() == null ? 0 : getPayload().limit();
        ByteBuffer buffer = ByteBuffer.allocate(5 + length);
        buffer.put((byte)(getFrame() << 4 | getCodec()));
        buffer.put(getPacketType());
        buffer.put(new byte[]{0x00, 0x00, 0x00});
        if (0 < length) {
            buffer.put(getPayload());
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
}
