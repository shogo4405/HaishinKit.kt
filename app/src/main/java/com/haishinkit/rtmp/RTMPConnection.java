package com.haishinkit.rtmp;

import android.util.Log;

import com.haishinkit.rtmp.message.RTMPCommandMessage;
import com.haishinkit.rtmp.message.RTMPMessage;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;

public class RTMPConnection {
    public static final int DEFAULT_PORT = 1935;
    public static final String DEFAULT_FLASH_VER = "FMLE/3.0 (compatible; FMSc/1.0)";
    public static final RTMPObjectEncoding DEFAULT_OBJECT_ENCODING = RTMPObjectEncoding.AMF0;

    private static final int DEFAULT_CHUNK_SIZE = 1024 * 8;
    private static final int DEFAULT_CAPABILITIES = 239;

    boolean connected = false;
    private int transactionID = 0;
    private Object[] arguments = null;
    private RTMPSocket socket = new RTMPSocket(this);

    public boolean isConnected() {
        return connected;
    }

    public void connect(final String command, Object... arguments) {
        if (connected) {
            return;
        }
        URI uri = URI.create(command);
        this.arguments = arguments;
        socket.connect(command, 1935);
    }

    public void close() {
        if (!connected) {
            return;
        }
        socket.close();
    }

    void listen(ByteBuffer buffer) {
        Log.e(getClass().getName(), buffer.toString());
    }

    RTMPMessage createConnectionMessage() {
        Map<String, Object> commandObject = new HashMap<String, Object>();
        RTMPCommandMessage message = new RTMPCommandMessage();
        message.setCommandName("connect");
        message.setTransactionID(transactionID++);
        message.setCommandObject(commandObject);
        return message;
    }
}
