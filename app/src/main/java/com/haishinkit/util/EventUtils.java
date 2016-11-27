package com.haishinkit.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.haishinkit.events.Event;

public final class EventUtils {
    private EventUtils() {
    }

    public static Map<String, Object> toMap(final Event event) {
        Object data = event.getData();
        if (data == null || !(data instanceof Map)) {
            return new HashMap<String, Object>();
        }
        return (Map<String, Object>) data;
    }
}
