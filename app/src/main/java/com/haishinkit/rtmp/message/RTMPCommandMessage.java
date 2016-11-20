package com.haishinkit.rtmp.message;

import java.util.Map;
import java.util.List;
import java.nio.ByteBuffer;

import com.haishinkit.amf.AMF0Serializer;
import com.haishinkit.rtmp.RTMPObjectEncoding;
import com.haishinkit.rtmp.RTMPSocket;

public final class RTMPCommandMessage extends RTMPMessage {
    private static final int CAPACITY = 1024;

    private final RTMPObjectEncoding objectEncoding;
    private String commandName = null;
    private int transactionID = 0;
    private Map<String, Object> commandObject = null;
    private List<Object> arguments = null;

    public RTMPCommandMessage(final RTMPObjectEncoding objectEncoding) {
        super(objectEncoding.getCommandType());
        this.objectEncoding = objectEncoding;
    }

    public RTMPObjectEncoding getObjectEncoding() {
        return objectEncoding;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public Map<String, Object> getCommandObject() {
        return commandObject;
    }

    public void setCommandObject(Map<String, Object> commandObject) {
        this.commandObject = commandObject;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }

    public ByteBuffer encode(RTMPSocket socket) {
        ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        if (getType().equals(Type.AMF3_COMMAND)) {
            buffer.put((byte) 0x00);
        }
        AMF0Serializer serializer = new AMF0Serializer(buffer);
        serializer.putString(getCommandName());
        serializer.putDouble((double) getTransactionID());
        serializer.putMap(getCommandObject());
        if (getArguments() != null) {
            for (Object object : getArguments()) {
                serializer.putObject(object);
            }
        }
        return buffer;
    }
}


