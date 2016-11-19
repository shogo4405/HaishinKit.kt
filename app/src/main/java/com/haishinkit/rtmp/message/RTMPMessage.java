package com.haishinkit.rtmp.message;

import com.haishinkit.rtmp.RTMPConnection;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.nio.ByteBuffer;

public abstract class RTMPMessage {

    public enum Type {
        CHUNK_SIZE((short) 1),
        ABORT((short) 2),
        ACK((short) 3),
        USER((short) 4),
        WINDOW_ACK((short) 5),
        BANDWIDTH((short) 6),
        AUDIO((short) 8),
        VIDEO((short) 9),
        AMF3_DATA((short) 15),
        AMF3_SHARED((short) 16),
        AMF3_COMMAND((short) 17),
        AMF0_DATA((short) 18),
        AMF0_SHARED((short) 19),
        AMF0_COMMAND((short) 20),
        AGGREGATE((short) 22),
        UNKNOWN((short) 255);

        private final short value;
        Type(final short value) {
            this.value = value;
        }
        public short valueOf() {
            return value;
        }
    }

    private final Type type;
    private int streamID = 0;
    private int timestamp = 0;

    public RTMPMessage(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public int getStreamID() {
        return streamID;
    }

    public void setStreamID(int streamID) {
        this.streamID = streamID;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public abstract ByteBuffer encode(RTMPConnection connection);

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
