package com.haishinkit.amf;

import android.util.Log;

import com.haishinkit.amf.data.ASXMLDocument;
import com.haishinkit.amf.data.ASArray;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.IllegalFormatFlagsException;
import java.nio.ByteBuffer;

public final class AMF0Deserializer {
    private final ByteBuffer buffer;

    public AMF0Deserializer(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public Object getObject() {
        switch (buffer.get()) {
            case 0x00: // number
                buffer.position(buffer.position() - 1);
                return getDouble();
            case 0x01: // bool
                buffer.position(buffer.position() - 1);
                return getBoolean();
            case 0x02: // string
                buffer.position(buffer.position() - 1);
                return getString();
            case 0x03: // object
                buffer.position(buffer.position() - 1);
                return getMap();
            case 0x04: // movieclip
                throw new UnsupportedOperationException();
            case 0x05: // null
                return null;
            case 0x06: // undefined
                return ASArray.ASUndefined.getInstance();
            case 0x07: // reference
                throw new UnsupportedOperationException();
            case 0x08: // ecmaarray
                buffer.position(buffer.position() - 1);
                return getList();
            case 0x09: // objectend
                throw new UnsupportedOperationException();
            case 0x0a: // strictarray
                throw new UnsupportedOperationException();
            case 0x0b: // date
                buffer.position(buffer.position() - 1);
                return getDate();
            case 0x0c: // longstring
                buffer.position(buffer.position() - 1);
                return getString();
            case 0x0d: // unsupported
                throw new UnsupportedOperationException();
            case 0x0e: // recordset
                throw new UnsupportedOperationException();
            case 0x0f: // xmldocument
                buffer.position(buffer.position() - 1);
                return getXMLDocument();
            case 0x10: // typedobject
                throw new UnsupportedOperationException();
            case 0x11: // avmplush
                throw new UnsupportedOperationException();
            default:
                break;
        }

        return null;
    }

    public boolean getBoolean() {
        byte marker = buffer.get();
        if (marker != AMF0Marker.BOOL.valueOf()) {
            throw new IllegalFormatFlagsException(new Byte(marker).toString());
        }
        return buffer.get() == 1;
    }

    public double getDouble() {
        byte marker = buffer.get();
        if (marker != AMF0Marker.NUMBER.valueOf()) {
            throw new IllegalFormatFlagsException(new Byte(marker).toString());
        }
        return buffer.getDouble();
    }

    public String getString() {
        byte marker = buffer.get();
        switch (marker) {
            case 0x02:
            case 0x0c:
                break;
            default:
                throw new IllegalFormatFlagsException(new Byte(marker).toString());
        }
        return getString(AMF0Marker.STRING.valueOf() == marker);
    }

    public Map<String, Object> getMap() {
        byte marker = buffer.get();
        if (marker == AMF0Marker.NULL.valueOf()) {
            return null;
        }
        if (marker != AMF0Marker.OBJECT.valueOf()) {
            throw new IllegalFormatFlagsException(new Byte(marker).toString());
        }
        Map<String, Object> map = new HashMap<String, Object>();
        while (true) {
            String key = getString(true);
            if (key.equals("")) {
                buffer.get();
                break;
            }
            map.put(key, getObject());
        }
        return map;
    }

    public List<Object> getList() {
        byte marker = buffer.get();
        if (marker == AMF0Marker.NULL.valueOf()) {
            return null;
        }
        if (marker != AMF0Marker.ECMAARRAY.valueOf()) {
            throw new IllegalFormatFlagsException(new Byte(marker).toString());
        }
        int count = buffer.getInt();
        ASArray array = new ASArray(count);
        while (true) {
            String key = getString(true);
            System.out.println(key);
            if (key.equals("")) {
                buffer.get();
                break;
            }
            array.put(key, getObject());
        }
        return array;
    }

    public Date getDate() {
        byte marker = buffer.get();
        if (marker != AMF0Marker.DATE.valueOf()) {
            throw new IllegalFormatFlagsException(new Byte(marker).toString());
        }
        double value = buffer.getDouble();
        buffer.position(buffer.position() + 2); // timezone
        Date date = new Date();
        date.setTime(new Double(value).longValue());
        return date;
    }

    public ASXMLDocument getXMLDocument() {
        byte marker = buffer.get();
        if (marker != AMF0Marker.XMLDOCUMENT.valueOf()) {
            throw new IllegalFormatFlagsException(new Byte(marker).toString());
        }
        return new ASXMLDocument(getString(false));
    }

    private String getString(final boolean asShort) {
        int length = 0;
        if (asShort) {
            length = buffer.getShort();
        } else {
            length = buffer.getInt();
        }
        try {
            byte[] bytes = new byte[length];
            buffer.get(bytes);
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(getClass().getName(), e.toString());
            return "";
        }
    }
}

