package com.haishinkit.rtmp.message;

import java.nio.ByteBuffer;
import com.haishinkit.rtmp.RTMPSocket;

public final class RTMPSetChunkSizeMessage extends RTMPMessage {
    private static final int CAPACITY = 4;

    private int size = 0;

    public RTMPSetChunkSizeMessage() {
        super(Type.CHUNK_SIZE);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public ByteBuffer encode(RTMPSocket socket) {
        if (socket == null) {
            throw new IllegalArgumentException();
        }
        ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        buffer.putInt(getSize());
        return buffer;
    }
}
