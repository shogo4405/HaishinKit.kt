package com.haishinkit.amf.data;

public final class ASUndefined {
    private static ASUndefined ourInstance = new ASUndefined();

    public static ASUndefined getInstance() {
        return ourInstance;
    }

    private ASUndefined() {
    }

    public String toString() {
        return "undefined";
    }
}