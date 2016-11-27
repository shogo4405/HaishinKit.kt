package com.haishinkit.rtmp.messages;

import com.haishinkit.rtmp.RTMPChunk;
import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPSocket;

import java.nio.ByteBuffer;

public final class RTMPWindowAcknowledgementSizeMessage extends RTMPMessage {
    private static final int CAPACITY = 4;

    private int size = 0;

    public RTMPWindowAcknowledgementSizeMessage() {
        super(Type.ACK);
    }

    public int getSize() {
        return size;
    }

    public RTMPWindowAcknowledgementSizeMessage setSize(final int size) {
        this.size = size;
        return this;
    }

    @Override
    public ByteBuffer encode(final RTMPSocket socket)  {
        ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        buffer.putInt(size);
        return buffer;
    }

    @Override
    public RTMPMessage decode(final RTMPSocket socket, final ByteBuffer buffer) {
        return setSize(buffer.getInt());
    }

    @Override
    public RTMPMessage execute(final RTMPConnection connection) {
        connection.getSocket().doOutput(
                RTMPChunk.ZERO,
                new RTMPWindowAcknowledgementSizeMessage()
                        .setSize(size)
                        .setChunkStreamID(RTMPChunk.CONTROL)
        );
        return this;
    }
}
