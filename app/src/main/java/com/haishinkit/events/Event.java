package com.haishinkit.events;

import org.apache.commons.lang3.builder.ToStringBuilder;

public final class Event {
    public static final String RTMP_STATUS = "rtmpStatus";

    String type = null;
    IEventDispatcher target = null;
    IEventDispatcher currentTarget = null;
    EventPhase eventPhase = EventPhase.NONE;
    Object data = null;
    boolean bubbles = false;
    boolean propagationStopped = false;

    public Event(final String type, final boolean bubbles, final Object data) {
        this.type = type;
        this.data = data;
        this.bubbles = bubbles;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public boolean isBubbles() {
        return bubbles;
    }

    public IEventDispatcher getTarget() {
        return target;
    }

    public IEventDispatcher getCurrentTarget() {
        return currentTarget;
    }

    public void stopPropagation() {
        propagationStopped = true;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
