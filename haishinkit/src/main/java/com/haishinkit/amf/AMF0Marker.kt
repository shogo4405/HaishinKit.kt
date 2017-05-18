package com.haishinkit.amf

internal enum class AMF0Marker(val rawValue: Byte) {
    NUMBER(0x00),
    BOOL(0x01),
    STRING(0x02),
    OBJECT(0x03),
    MOVIECLIP(0x04),
    NULL(0x05),
    UNDEFINED(0x06),
    REFERENCE(0x07),
    ECMAARRAY(0x08),
    OBJECTEND(0x09),
    STRICTARRAY(0x0a),
    DATE(0x0b),
    LONGSTRING(0x0c),
    UNSUPPORTED(0x0d),
    RECORDSET(0x0e),
    XMLDOCUMENT(0x0f),
    TYPEDOBJECT(0x10),
    AVMPLUSH(0x11);
}
