package com.haishinkit.rtmp;

import com.haishinkit.rtmp.message.RTMPSetChunkSizeMessage;
import com.haishinkit.util.ByteBufferUtils;

import org.junit.Test;
import java.util.List;
import java.nio.ByteBuffer;
import static org.junit.Assert.*;

public class RTMPChunkTest {
    @Test
    public void encode() {
        RTMPSetChunkSizeMessage message = new RTMPSetChunkSizeMessage();
        message.setChunkStreamID((short) 1);
        message.setStreamID(1);
        message.setSize(1024);

        RTMPConnection connection = new RTMPConnection();
        RTMPSocket socket = new RTMPSocket(connection);

        List<ByteBuffer> list = RTMPChunk.ZERO.encode(socket, message);
        for (ByteBuffer buffer : list) {
            System.out.println(ByteBufferUtils.toHexString(buffer));
        }
    }
}
