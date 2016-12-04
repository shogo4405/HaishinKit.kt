package com.haishinkit.rtmp;

import java.util.List;
import java.util.ArrayList;
import java.nio.ByteBuffer;

import com.haishinkit.lang.IRawValue;
import com.haishinkit.rtmp.messages.RTMPMessage;

public enum RTMPChunk implements IRawValue<Byte> {
    ZERO((byte) 0x00),
    ONE((byte) 0x01),
    TWO((byte) 0x02),
    THREE((byte) 0x03);

    public static final short CONTROL = 0x02;
    public static final short COMMAND = 0x03;
    public static final short AUDIO = 0x04;
    public static final short VIDEO = 0x05;
    public static final int DEFAULT_SIZE = 128;

    public static RTMPChunk rawValue(final byte rawValue) {
        switch (rawValue) {
            case 0x00:
                return RTMPChunk.ZERO;
            case 0x01:
                return RTMPChunk.ONE;
            case 0x02:
                return RTMPChunk.TWO;
            case 0x03:
                return RTMPChunk.THREE;
        }
        return null;
    }

    private final byte rawValue;

    RTMPChunk(final byte rawValue) {
        this.rawValue = rawValue;
    }

    public Byte rawValue() {
        return rawValue;
    }

    public List<ByteBuffer> encode(RTMPSocket socket, RTMPMessage message) {
        if (socket == null || message == null) {
            throw new IllegalArgumentException();
        }

        final ByteBuffer payload = message.encode(socket);
        payload.flip();

        final List<ByteBuffer> list = new ArrayList<ByteBuffer>();
        final int length = payload.limit();
        final int timestamp = message.getTimestamp();
        final int chunkSize = socket.getChunkSizeS();
        ByteBuffer buffer = ByteBuffer.allocate(length(message.getChunkStreamID()) + (length < chunkSize ? length : chunkSize));
        message.setLength(length);

        buffer.put(header(message.getChunkStreamID()));
        buffer.put(new byte[]{(byte)(timestamp >> 16), (byte)(timestamp >> 8), (byte) timestamp});

        switch (this) {
            case ZERO:
                buffer.put(new byte[]{(byte)(length >> 16), (byte)(length >> 8), (byte) length});
                buffer.put(message.getType().rawValue());
                int streamID = message.getStreamID();
                // message streamID is a litleEndian
                buffer.put(new byte[]{(byte) streamID, (byte)(streamID >> 8), (byte)(streamID >> 16), (byte)(streamID >> 24)});
                break;
            case ONE:
                buffer.put(new byte[]{(byte)(length >> 16), (byte)(length >> 8), (byte)length});
                buffer.put(message.getType().rawValue());
                break;
            default:
                break;
        }

        if (length < chunkSize) {
            buffer.put(payload.array(), 0, length);
            buffer.flip();
            list.add(buffer);
            return list;
        }

        final int mod = length % chunkSize;
        final byte[] three = RTMPChunk.THREE.header(message.getChunkStreamID());
        buffer.put(payload.array(), 0, chunkSize);
        list.add(buffer);
        for (int i = 1; i < (length - mod) / chunkSize; ++i) {
            buffer = ByteBuffer.allocate(three.length + chunkSize);
            buffer.put(three);
            buffer.put(payload.array(), chunkSize * i, chunkSize);
            list.add(buffer);
        }
        buffer = ByteBuffer.allocate(three.length + mod);
        buffer.put(three);
        buffer.put(payload.array(), length - mod, mod);
        list.add(buffer);

        return list;
    }

    public RTMPMessage decode(final short chunkStreamID, final RTMPConnection connection, final ByteBuffer buffer) {
        if (connection == null || buffer == null) {
            throw new IllegalArgumentException();
        }

        int timestamp = 0;
        int length = 0;
        byte type = 0;
        int streamID = 0;

        switch (this) {
            case ZERO:
                timestamp = getInt(buffer);
                length = getInt(buffer);
                type = buffer.get();
                streamID = Integer.reverseBytes(buffer.getInt());
                break;
            case ONE:
                timestamp = getInt(buffer);
                length = getInt(buffer);
                type = buffer.get();
                break;
            case TWO:
                timestamp = getInt(buffer);
                return connection.getMessages().get(chunkStreamID).setTimestamp(timestamp);
            default:
                break;
        }

        return RTMPMessage
                .create(type)
                .setChunkStreamID(chunkStreamID)
                .setStreamID(streamID)
                .setTimestamp(timestamp)
                .setLength(length);
    }

    public int length(short streamID) {
        int basic = 3;
        if (streamID <= 63) {
            basic = 1;
        } else if (streamID <= 319) {
            basic = 2;
        }
        switch (this) {
            case ZERO:
                return basic + 11;
            case ONE:
                return basic + 7;
            case TWO:
                return basic + 3;
            case THREE:
                return basic + 0;
            default:
                return 0;
        }
    }

    public byte[] header(short streamID) {
        if (streamID <= 63) {
            return new byte[]{(byte) (rawValue << 6 | (short) streamID)};
        }
        if (streamID <= 319) {
            return new byte[]{(byte) (rawValue << 6 | 0b0000000), (byte) (streamID - 64)};
        }
        return new byte[]{(byte) (rawValue << 6 | 0b00111111), (byte) ((streamID - 64) >> 8), (byte) (streamID - 64)};
    }

    public int getInt(final ByteBuffer buffer) {
        byte[] bytes = new byte[3];
        buffer.get(bytes);
        return (int) (bytes[0] & 0XFF) << 16 | (int) (bytes[1] & 0xFF) << 8 | (int) (bytes[2] & 0xFF);
    }

    public short getStreamID(final ByteBuffer buffer) {
        buffer.position(buffer.position() - 1);
        byte first = buffer.get();
        byte[] bytes = null;
        switch (first & 0b00111111) {
            case 0:
                bytes = new byte[2];
                buffer.get(bytes);
                return (short)(bytes[1] + 64);
            case 1:
                bytes = new byte[3];
                buffer.get(bytes);
                return (short) ((short)(bytes[1]) << 8 | (short) (bytes[2]) | (short) 64);
            default:
                return (short) (first & 0b00111111);
        }
    }
}
