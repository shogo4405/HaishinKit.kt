package com.haishinkit.amf;

public enum AMF0Marker {
    NUMBER((byte) 0x00),
    BOOL((byte) 0x01),
    STRING((byte) 0x02),
    OBJECT((byte) 0x03),
    MOVIECLIP((byte) 0x04),
    NULL((byte) 0x05),
    UNDEFINED((byte) 0x06),
    REFERENCE((byte) 0x07),
    ECMAARRAY((byte) 0x08),
    OBJECTEND((byte) 0x09),
    STRICTARRAY((byte) 0x0a),
    DATE((byte) 0x0b),
    LONGSTRING((byte) 0x0c),
    UNSUPPORTED((byte) 0x0d),
    RECORDSET((byte) 0x0e),
    XMLDOCUMENT((byte) 0x0f),
    TYPEDOBJECT((byte) 0x10),
    AVMPLUSH((byte) 0x11);

    private final byte value;

    AMF0Marker(final byte value) {
        this.value = value;
    }

    public byte valueOf() {
        return value;
    }
}
