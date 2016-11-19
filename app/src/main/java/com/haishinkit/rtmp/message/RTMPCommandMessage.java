package com.haishinkit.rtmp.message;

import java.util.Map;
import java.util.List;
import com.haishinkit.rtmp.RTMPObjectEncoding;

public final class RTMPCommandMessage extends RTMPMessage {
    private RTMPObjectEncoding objectEncoding = RTMPObjectEncoding.AMF0;
    private String commandName = null;
    private int transactionID = 0;
    private Map<String, Object> commandObject = null;
    private List<Object> arguments = null;

    public RTMPObjectEncoding getObjectEncoding() {
        return objectEncoding;
    }

    public void setObjectEncoding(RTMPObjectEncoding objectEncoding) {
        this.objectEncoding = objectEncoding;
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
}
