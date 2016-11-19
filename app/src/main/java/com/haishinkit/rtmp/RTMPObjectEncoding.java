package com.haishinkit.rtmp;

import com.haishinkit.rtmp.message.RTMPCommandMessage;
import com.haishinkit.rtmp.message.RTMPMessage;

public enum RTMPObjectEncoding {
    AMF0((short) 0, RTMPMessage.Type.AMF0_COMMAND),
    AMF3((short) 3, RTMPMessage.Type.AMF3_COMMAND);

    private final short value;
    private final RTMPMessage.Type messageType;

    RTMPObjectEncoding(short value, RTMPMessage.Type messageType) {
        this.value = value;
        this.messageType = messageType;
    }

    public short valueOf() {
        return value;
    }

    public RTMPMessage.Type getMessageType() {
        return messageType;
    }
}
