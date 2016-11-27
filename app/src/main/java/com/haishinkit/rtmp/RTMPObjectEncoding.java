package com.haishinkit.rtmp;

import com.haishinkit.rtmp.messages.RTMPMessage;

public enum RTMPObjectEncoding {
    AMF0((short) 0, RTMPMessage.Type.AMF0_DATA, RTMPMessage.Type.AMF0_SHARED, RTMPMessage.Type.AMF0_COMMAND),
    AMF3((short) 3, RTMPMessage.Type.AMF3_DATA, RTMPMessage.Type.AMF3_SHARED, RTMPMessage.Type.AMF3_COMMAND);

    private final short value;
    private final RTMPMessage.Type dataType;
    private final RTMPMessage.Type sharedObjectType;
    private final RTMPMessage.Type commandType;

    RTMPObjectEncoding(final short value, final RTMPMessage.Type dataType, final RTMPMessage.Type sharedObjectType, final RTMPMessage.Type commandType) {
        this.value = value;
        this.dataType = dataType;
        this.sharedObjectType = sharedObjectType;
        this.commandType = commandType;
    }

    public short valueOf() {
        return value;
    }

    public RTMPMessage.Type getDataType() {
        return dataType;
    }

    public RTMPMessage.Type getSharedObjectType() {
        return sharedObjectType;
    }

    public RTMPMessage.Type getCommandType() {
        return commandType;
    }
}
