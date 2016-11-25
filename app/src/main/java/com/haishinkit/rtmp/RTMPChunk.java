package com.haishinkit.rtmp;

import java.util.List;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import com.haishinkit.rtmp.message.RTMPMessage;
import com.haishinkit.util.ByteBufferUtils;

import org.jetbrains.annotations.Contract;

public enum RTMPChunk {
    ZERO((byte) 0),
    ONE((byte) 1),
    TWO((byte) 2),
    THREE((byte) 3);

    public static final short CONTROL = 0x02;
    public static final short COMMAND = 0x03;
    public static final short AUDIO = 0x04;
    public static final short VIDEO = 0x05;
    public static final int DEFAULT_SIZE = 128;

    private final byte value;

    RTMPChunk(final byte value) {
        this.value = value;
    }

    public byte valueOf() {
        return value;
    }

    public List<ByteBuffer> encode(RTMPSocket socket, RTMPMessage message) {
        if (socket == null || message == null) {
            throw new IllegalArgumentException();
        }

        ByteBuffer payload = message.encode(socket);
        payload.flip();

        List<ByteBuffer> list = new ArrayList<ByteBuffer>();
        int length = payload.limit();
        int timestamp = message.getTimestamp();
        int chunkSize = socket.getChunkSizeC();
        ByteBuffer buffer = ByteBuffer.allocate(length(message.getChunkStreamID()) + (length < chunkSize ? length : chunkSize));
        message.setLength(length);

        buffer.put(header(message.getChunkStreamID()));
        buffer.put(new byte[]{(byte)(timestamp >> 16), (byte)(timestamp >> 8), (byte) timestamp});

        switch (this) {
            case ZERO:
                buffer.put(new byte[]{(byte)(length >> 16), (byte)(length >> 8), (byte) length});
                buffer.put(message.getType().valueOf());
                int streamID = message.getStreamID();
                // message streamID is a litleEndian
                buffer.put(new byte[]{(byte) streamID, (byte)(streamID >> 8), (byte)(streamID >> 16), (byte)(streamID >> 24)});
                break;
            case ONE:
                buffer.put(new byte[]{(byte)(length >> 16), (byte)(length >> 8), (byte)length});
                buffer.put(message.getType().valueOf());
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

        int mod = length % chunkSize;
        byte[] three = RTMPChunk.THREE.header(message.getChunkStreamID());
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
            return new byte[]{(byte) (value << 6 | (short) streamID)};
        }
        if (streamID <= 319) {
            return new byte[]{(byte) (value << 6 | 0b0000000), (byte) (streamID - 64)};
        }
        return new byte[]{(byte) (value << 6 | 0b00111111), (byte) ((streamID - 64) >> 8), (byte) (streamID - 64)};
    }
}
