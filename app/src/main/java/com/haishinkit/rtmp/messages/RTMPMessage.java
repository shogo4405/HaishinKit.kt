package com.haishinkit.rtmp.messages;

import com.haishinkit.lang.IRawValue;
import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPObjectEncoding;
import com.haishinkit.rtmp.RTMPSocket;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import java.nio.ByteBuffer;

public class RTMPMessage {

    public enum Type implements IRawValue<Byte> {
        CHUNK_SIZE((byte) 0x01),
        ABORT((byte) 0x02),
        ACK((byte) 0x03),
        USER((byte) 0x04),
        WINDOW_ACK((byte) 0x05),
        BANDWIDTH((byte) 0x06),
        AUDIO((byte) 0x08),
        VIDEO((byte) 0x09),
        AMF3_DATA((byte) 0x0F),
        AMF3_SHARED((byte) 0x10),
        AMF3_COMMAND((byte) 0x11),
        AMF0_DATA((byte) 0x12),
        AMF0_SHARED((byte) 0x13),
        AMF0_COMMAND((byte) 0x14),
        AGGREGATE((byte) 0x16),
        UNKNOWN((byte) 0xFF);

        private final byte rawValue;

        Type(final byte rawValue) {
            this.rawValue = rawValue;
        }

        public Byte rawValue() {
            return rawValue;
        }
    }

    public final static RTMPMessage create(final byte value) {
        switch (value) {
            case 0x01:
                return new RTMPSetChunkSizeMessage();
            case 0x02:
                return new RTMPAbortMessage();
            case 0x03:
                return new RTMPAcknowledgementMessage();
            case 0x04:
                return new RTMPUserControlMessage();
            case 0x05:
                return new RTMPWindowAcknowledgementSizeMessage();
            case 0x06:
                return new RTMPSetPeerBandwidthMessage();
            case 0x08:
                return new RTMPAudioMessage();
            case 0x09:
                return new RTMPVideoMessage();
            case 0x12:
                return new RTMPDataMessage(RTMPObjectEncoding.AMF0);
            case 0x14:
                return new RTMPCommandMessage(RTMPObjectEncoding.AMF0);
            default:
                return new RTMPMessage(Type.UNKNOWN);
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

    public RTMPMessage setStreamID(int streamID) {
        this.streamID = streamID;
        return this;
    }

    public short getChunkStreamID() {
        return this.chunkStreamID;
    }

    public RTMPMessage setChunkStreamID(final short chunkStreamID) {
        this.chunkStreamID = chunkStreamID;
        return this;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public RTMPMessage setTimestamp(final int timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public int getLength() {
        return length;
    }

    public RTMPMessage setLength(final int length) {
        this.length = length;
        return this;
    }

    public ByteBuffer encode(final RTMPSocket socket) {
        throw new NotImplementedException(getClass().getName() + "#encode");
    }

    public RTMPMessage decode(final ByteBuffer buffer) {
        throw new NotImplementedException(getClass().getName() + "#decode");
    }

    public RTMPMessage execute(final RTMPConnection connection) {
        throw new NotImplementedException(getClass().getName() + "#execute");
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
