package com.haishinkit.rtmp;

public enum RTMPObjectEncoding {
    AMF0((short) 0),
    AMF3((short) 3);

    private short value = 0;

    public short valueOf() {
        return value;
    }

    RTMPObjectEncoding(short value) {
        this.value = value;
    }
}
