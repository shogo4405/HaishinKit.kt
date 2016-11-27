package com.haishinkit.rtmp;

import com.haishinkit.lang.IRawValue;
import com.haishinkit.rtmp.messages.RTMPMessage;

public enum RTMPObjectEncoding implements IRawValue<Short> {
    AMF0((short) 0, RTMPMessage.Type.AMF0_DATA, RTMPMessage.Type.AMF0_SHARED, RTMPMessage.Type.AMF0_COMMAND),
    AMF3((short) 3, RTMPMessage.Type.AMF3_DATA, RTMPMessage.Type.AMF3_SHARED, RTMPMessage.Type.AMF3_COMMAND);

    private final short rawValue;
    private final RTMPMessage.Type dataType;
    private final RTMPMessage.Type sharedObjectType;
    private final RTMPMessage.Type commandType;

    RTMPObjectEncoding(final short rawValue, final RTMPMessage.Type dataType, final RTMPMessage.Type sharedObjectType, final RTMPMessage.Type commandType) {
        this.rawValue = rawValue;
        this.dataType = dataType;
        this.sharedObjectType = sharedObjectType;
        this.commandType = commandType;
    }

    public Short rawValue() {
        return rawValue;
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
