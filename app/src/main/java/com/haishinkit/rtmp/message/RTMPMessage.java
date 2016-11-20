package com.haishinkit.rtmp.message;

import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPSocket;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.nio.ByteBuffer;

public abstract class RTMPMessage {

    public enum Type {
        CHUNK_SIZE((byte) 1),
        ABORT((byte) 2),
        ACK((byte) 3),
        USER((byte) 4),
        WINDOW_ACK((byte) 5),
        BANDWIDTH((byte) 6),
        AUDIO((byte) 8),
        VIDEO((byte) 9),
        AMF3_DATA((byte) 15),
        AMF3_SHARED((byte) 16),
        AMF3_COMMAND((byte) 17),
        AMF0_DATA((byte) 18),
        AMF0_SHARED((byte) 19),
        AMF0_COMMAND((byte) 20),
        AGGREGATE((byte) 22),
        UNKNOWN((byte) 255);

        private final byte value;

        Type(final byte value) {
            this.value = value;
        }

        public byte valueOf() {
            return value;
        }
    }

    private final Type type;
    private short chunkStreamID = 0;
    private int streamID = 0;
    private int timestamp = 0;
    private int length = 0;

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

    public short getChunkStreamID() {
        return this.chunkStreamID;
    }

    public void setChunkStreamID(short chunkStreamID) {
        this.chunkStreamID = chunkStreamID;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public abstract ByteBuffer encode(RTMPSocket socket);

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
