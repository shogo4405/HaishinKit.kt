package com.haishinkit.events;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class EventDispatcher implements IEventDispatcher {
    private final IEventDispatcher target;
    private ConcurrentHashMap<String, List<IEventListener>> listeners = new ConcurrentHashMap<String, List<IEventListener>>();

    public EventDispatcher(final IEventDispatcher target) {
        this.target = target;
    }

    public void addEventListener(final String type, final IEventListener listener, final boolean useCapture) {
        String key = type + "/" + new Boolean(useCapture).toString();
        listeners.putIfAbsent(key, Collections.synchronizedList(new ArrayList<IEventListener>()));
        List<IEventListener> list = listeners.get(key);
        list.add(listener);
    }

    public void addEventListener(final String type, final IEventListener listener) {
        addEventListener(type, listener, false);
    }

    public void dispatchEvent(final Event event) {
        if (event.getType() == null) {
            throw new IllegalArgumentException();
        }

        List<IEventDispatcher> targets = new ArrayList<IEventDispatcher>();
        targets.add(target == null ? this : target);

        for (IEventDispatcher target : targets) {
            event.currentTarget = target;
            boolean isTargetPhase = target == event.target;

            if (isTargetPhase) {
                event.eventPhase = EventPhase.AT_TARGET;
            }

            String isCapturingPhase = new Boolean(event.eventPhase == EventPhase.CAPTURING).toString();
            List<IEventListener> listeners = this.listeners.get(event.type + "/" + isCapturingPhase);
            if (listeners != null) {
                for (IEventListener listener : listeners) {
                    listener.handleEvent(event);
                }
                if (event.propagationStopped) {
                    break;
                }
            }

            if (isTargetPhase) {
                event.eventPhase = EventPhase.BUBBLING;
            }
        }

        event.target = null;
        event.currentTarget = null;
        event.eventPhase = EventPhase.NONE;
        event.propagationStopped = false;
    }

    public void dispatchEventWith(final String type, final boolean bubbles, final Object data) {
        dispatchEvent(new Event(type, bubbles, data));
    }

    public void dispatchEventWith(final String type, final boolean bubbles) {
        dispatchEventWith(type, bubbles, null);
    }

    public void dispatchEventWith(final String type) {
        dispatchEventWith(type, false);
    }

    public void removeEventListener(final String type, final IEventListener listener, final boolean useCapture) {
        String key = type + "/" + new Boolean(useCapture).toString();
        if (!listeners.containsKey(key)) {
            return;
        }
        List<IEventListener> list = listeners.get(key);
        for (int i = list.size() - 1; 0 <= i; --i) {
            if (list.get(i) == listener) {
                list.remove(i);
                return;
            }
        }
    }

    public void removeEventListener(final String type, final IEventListener listener) {
        removeEventListener(type, listener, false);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
