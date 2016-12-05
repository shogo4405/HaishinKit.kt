package com.haishinkit.rtmp.messages;

import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPSocket;
import java.nio.ByteBuffer;

/**
 * 5.4.3 Acknowledgement (3)
 */
public final class RTMPAcknowledgementMessage extends RTMPMessage {
    private static final int CAPACITY = 4;

    private byte[] sequence = null;

    public RTMPAcknowledgementMessage() {
        super(Type.ACK);
        sequence = new byte[CAPACITY];
    }

    public final byte[] getSequence() {
        return sequence;
    }

    public final RTMPAcknowledgementMessage setSequence(final byte[] sequence) {
        this.sequence = sequence;
        return this;
    }

    @Override
    public ByteBuffer encode(final RTMPSocket socket)  {
        if (socket == null) {
            throw new IllegalArgumentException();
        }
        ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        buffer.put(sequence);
        return buffer;
    }

    @Override
    public RTMPMessage decode(final ByteBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        buffer.get(getSequence());
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
