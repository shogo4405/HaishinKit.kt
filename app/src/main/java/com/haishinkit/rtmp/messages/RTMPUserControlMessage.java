package com.haishinkit.rtmp.messages;

import com.haishinkit.lang.IRawValue;
import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPSocket;

import java.nio.ByteBuffer;

public final class RTMPUserControlMessage extends RTMPMessage {
    private static int CAPACITY = 6;

    public enum Event implements IRawValue<Short> {
        STREAM_BEGIN((short) 0x00),
        STREAM_EOF((short) 0x01),
        STREAM_DRY((short) 0x02),
        SET_BUFFER((short) 0x03),
        RECORDED((short) 0x04),
        PING((short) 0x05),
        PONG((short) 0x06),
        BUFFER_EMPTY((short) 0x1F),
        BUFFER_FULL((short) 0x20),
        UNKNOWN((short) 0xFF);

        public static Event rawValue(final short rawValue) {
            switch (rawValue) {
                case 0x00:
                    return STREAM_BEGIN;
                case 0x01:
                    return STREAM_EOF;
                case 0x02:
                    return STREAM_DRY;
                case 0x03:
                    return SET_BUFFER;
                case 0x04:
                    return RECORDED;
                case 0x05:
                    return PING;
                case 0x06:
                    return PONG;
                case 0x1F:
                    return BUFFER_EMPTY;
                case 0x20:
                    return BUFFER_FULL;
            }
            return UNKNOWN;
        }

        private final short rawValue;

        Event(final short rawValue) {
            this.rawValue = rawValue;
        }

        public Short rawValue() {
            return rawValue;
        }
    }

    private Event event = Event.UNKNOWN;
    private int value = 0;

    public RTMPUserControlMessage() {
        super(Type.USER);
    }

    public Event getEvent() {
        return event;
    }

    public RTMPUserControlMessage setEvent(final Event event) {
        this.event = event;
        return this;
    }

    public int getValue() {
        return value;
    }

    public RTMPUserControlMessage setValue(final int value) {
        this.value = value;
        return this;
    }

    @Override
    public ByteBuffer encode(final RTMPSocket socket) {
        ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        buffer.putShort(getEvent().rawValue());
        buffer.putInt(value);
        return buffer;
    }

    @Override
    public RTMPMessage decode(final RTMPSocket socket, final ByteBuffer buffer) {
        return setEvent(Event.rawValue(buffer.getShort())).setValue(buffer.getInt());
    }

    @Override
    public RTMPMessage execute(final RTMPConnection connection) {
        return this;
    }
}

