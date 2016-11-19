package com.haishinkit.rtmp.message;

public class RTMPMessage {

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

    private Type type = Type.UNKNOWN;
    private int streamID = 0;
    private int timestamp = 0;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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
}
