package com.haishinkit.amf;

import com.haishinkit.as3.ASUndefined;

import java.util.Map;
import java.util.List;
import java.util.Date;
import java.nio.ByteBuffer;

public final class AMF0Serializer {
    private final ByteBuffer buffer;

    public AMF0Serializer(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public AMF0Serializer putBoolean(final boolean value) {
        buffer.put(AMF0Marker.BOOL.valueOf());
        buffer.put((byte)(value ? 1 : 0));
        return this;
    }

    public AMF0Serializer putDouble(final double value) {
        buffer.put(AMF0Marker.NUMBER.valueOf());
        buffer.putDouble(value);
        return this;
    }

    public AMF0Serializer putString(final String value) {
        if (value == null) {
            buffer.put(AMF0Marker.NULL.valueOf());
            return this;
        }
        int length = value.length();
        boolean isShort = length <= (int) Short.MAX_VALUE ? true : false;
        buffer.put(isShort ? AMF0Marker.STRING.valueOf() : AMF0Marker.LONGSTRING.valueOf());
        return putString(value, isShort);
    }

    public AMF0Serializer putMap(final Map<String, Object> value) {
        if (value == null) {
            buffer.put(AMF0Marker.NULL.valueOf());
            return this;
        }
        buffer.put(AMF0Marker.OBJECT.valueOf());
        for (Map.Entry<String, Object> entry: value.entrySet()) {
            putString(entry.getKey(), true).putObject(entry.getValue());
        }
        putString("", true);
        buffer.put(AMF0Marker.OBJECTEND.valueOf());
        return this;
    }

    public AMF0Serializer putDate(final Date value) {
        if (value == null) {
            buffer.put(AMF0Marker.NULL.valueOf());
            return this;
        }
        buffer.put(AMF0Marker.DATE.valueOf());
        buffer.putDouble(new Long(value.getTime()).doubleValue());
        buffer.put(new byte[]{0x00, 0x00});
        return this;
    }

    public AMF0Serializer putList(final List<Object> value) {
        if (value == null) {
            buffer.put(AMF0Marker.NULL.valueOf());
            return this;
        }
        buffer.put(AMF0Marker.ECMAARRAY.valueOf());
        if (value.isEmpty()) {
            buffer.put(new byte[]{0x00, 0x00, 0x00, 0x00});
            return this;
        }
        buffer.putInt(value.size());
        for (Object object : value) {
            putObject(object);
        }
        return this;
    }

    public AMF0Serializer putObject(final Object value) {
        if (value == null) {
            buffer.put(AMF0Marker.NULL.valueOf());
            return this;
        }
        if (value instanceof String) {
            return putString((String) value);
        }
        if (value instanceof Double) {
            return putDouble((Double) value);
        }
        if (value instanceof Integer) {
            return putDouble(((Integer) value).doubleValue());
        }
        if (value instanceof Boolean) {
            return putBoolean((Boolean) value);
        }
        if (value instanceof Date) {
            return putDate((Date) value);
        }
        if (value instanceof Map) {
            return putMap((Map<String, Object>) value);
        }
        if (value instanceof List) {
            return putList((List<Object>) value);
        }
        if (value instanceof ASUndefined) {
            buffer.put(AMF0Marker.UNDEFINED.valueOf());
            return this;
        }
        return this;
    }

    public String toString() {
        return buffer.toString();
    }

    private AMF0Serializer putString(final String value, final boolean asShort) {
        int length = value.length();
        if (asShort) {
            buffer.putShort((short) length);
        } else {
            buffer.putInt(length);
        }
        buffer.put(value.getBytes());
        return this;
    }
}
