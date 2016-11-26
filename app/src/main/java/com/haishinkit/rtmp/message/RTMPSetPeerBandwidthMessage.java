package com.haishinkit.rtmp.message;

import com.haishinkit.lang.IRawValue;
import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPSocket;

import java.nio.ByteBuffer;

public final class RTMPSetPeerBandwidthMessage extends RTMPMessage {
    private static int CAPACITY = 5;

    public enum Limit implements IRawValue<Byte> {
        HARD((byte) 0x00),
        SOFT((byte) 0x01),
        DYNAMIC((byte) 0x10),
        UNKNOWN((byte) 0x00);

        public static Limit rawValue(final byte rawValue) {
            switch (rawValue) {
                case 0x00:
                    return HARD;
                case 0x01:
                    return SOFT;
                case 0x02:
                    return DYNAMIC;
                default:
                    return UNKNOWN;
            }
        }

        private final byte rawValue;

        Limit(final byte rawValue) {
            this.rawValue = rawValue;
        }

        public Byte rawValue() {
            return rawValue;
        }
    }

    private int size = 0;
    private Limit limit = Limit.HARD;

    public RTMPSetPeerBandwidthMessage() {
        super(Type.BANDWIDTH);
    }

    public int getSize() {
        return size;
    }

    public RTMPSetPeerBandwidthMessage setSize(final int size) {
        this.size = size;
        return this;
    }

    public Limit getLimit() {
        return limit;
    }

    public RTMPSetPeerBandwidthMessage setLimit(final Limit limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public ByteBuffer encode(final RTMPSocket socket) {
        ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        buffer.putInt(size);
        buffer.put(limit.rawValue());
        return buffer;
    }

    @Override
    public RTMPMessage decode(final RTMPSocket socket, final ByteBuffer buffer) {
        return setSize(buffer.getInt()).setLimit(Limit.rawValue(buffer.get()));
    }

    @Override
    public RTMPMessage execute(final RTMPConnection connection) {
        connection.getSocket().setBandwidth(getSize());
        return this;
    }
}
