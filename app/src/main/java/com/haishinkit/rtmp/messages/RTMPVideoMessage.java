package com.haishinkit.rtmp.messages;

import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPSocket;

import java.nio.ByteBuffer;

public final class RTMPVideoMessage extends RTMPMessage {
    public RTMPVideoMessage() {
        super(Type.VIDEO);
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
