package com.haishinkit.rtmp;

import com.haishinkit.rtmp.messages.RTMPCommandMessage;
import com.haishinkit.rtmp.messages.RTMPSetChunkSizeMessage;
import com.haishinkit.util.ByteBufferUtils;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RTMPChunkTests {
    @Test
    public void encodeSetChunkSize() {
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

    @Test
    public void encodeCommand() {
        Map<String, Object> commandObject = new HashMap<String, Object>();
        commandObject.put("app", "");
        commandObject.put("flashVer", "MAC10");
        commandObject.put("swfUrl", "http://localhost/hoge.swf");
        commandObject.put("tcUrl", "rtmp://localhost/appName/instanceName/");
        commandObject.put("fpad", false);
        commandObject.put("capabilities", 10);
        commandObject.put("audioCodecs", 10);
        commandObject.put("videoCodecs", 10);
        commandObject.put("videoFunction", 1);
        commandObject.put("pageUrl", "http://localhost/");
        commandObject.put("objectEncoding", 3);
        commandObject.put("timestamp", new Date());

        RTMPCommandMessage message = new RTMPCommandMessage(RTMPObjectEncoding.AMF0);
        message.setChunkStreamID((short) 2);
        message.setCommandName("connect");
        message.setCommandObject(commandObject);

        RTMPConnection connection = new RTMPConnection();
        RTMPSocket socket = new RTMPSocket(connection);
        socket.setChunkSizeC(1024);

        List<ByteBuffer> list = RTMPChunk.ZERO.encode(socket, message);
        for (ByteBuffer buffer : list) {
            System.out.println(ByteBufferUtils.toHexString(buffer));
        }
    }
}
