package com.haishinkit.as3;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public final class ASArray extends ArrayList<Object> {
    private Map<String, Object> properties = new HashMap<String, Object>();

    public ASArray(int capacity) {
        super(capacity);
    }

    public void put(String k, Object v) {
        properties.put(k, v);
    }

    public String toString() {
        return "{" + super.toString() + "," + properties.toString() + "}";
    }
}
