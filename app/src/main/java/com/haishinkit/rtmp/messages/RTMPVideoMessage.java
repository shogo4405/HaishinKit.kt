package com.haishinkit.rtmp.messages;

import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPSocket;

import java.nio.ByteBuffer;

public class RTMPVideoMessage extends RTMPMessage {
    private byte frame = 0x00;
    private byte codec = 0x00;
    private ByteBuffer payload = null;

    public RTMPVideoMessage() {
        super(Type.VIDEO);
    }

    public byte getFrame() {
        return frame;
    }

    public RTMPVideoMessage setFrame(final byte frame) {
        this.frame = frame;
        return this;
    }

    public byte getCodec() {
        return codec;
    }

    public RTMPVideoMessage setCodec(final byte codec) {
        this.codec = codec;
        return this;
    }

    public ByteBuffer getPayload() {
        return payload;
    }

    public RTMPVideoMessage setPayload(final ByteBuffer payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public ByteBuffer encode(final RTMPSocket socket) {
        if (socket == null) {
            throw new IllegalArgumentException();
        }
        int length = getPayload() == null ? 0 : getPayload().limit();
        ByteBuffer buffer = ByteBuffer.allocate(1 + length);
        buffer.put((byte)(getFrame() << 4 | getCodec()));
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

    @Override
    public RTMPMessage execute(final RTMPConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException();
        }
        return this;
    }
}
