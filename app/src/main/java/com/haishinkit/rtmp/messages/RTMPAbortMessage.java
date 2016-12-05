package com.haishinkit.rtmp.messages;

import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPSocket;

import java.nio.ByteBuffer;

public final class RTMPAbortMessage extends RTMPMessage {
    private static final int CAPACITY = 4;

    private int discarded = 0;

    public RTMPAbortMessage() {
        super(Type.ABORT);
    }

    public final int getDiscarded() {
        return discarded;
    }

    public final RTMPAbortMessage setDiscarded(final int discarded) {
        this.discarded = discarded;
        return this;
    }

    @Override
    public ByteBuffer encode(final RTMPSocket socket)  {
        if (socket == null) {
            throw new IllegalArgumentException();
        }
        ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        buffer.putInt(getDiscarded());
        return buffer;
    }

    @Override
    public RTMPMessage decode(final ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        return setDiscarded(buffer.getInt());
    }

    @Override
    public RTMPMessage execute(final RTMPConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException();
        }
        return this;
    }
}
