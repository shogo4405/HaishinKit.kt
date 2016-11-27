package com.haishinkit.events;

public interface IEventDispatcher {
    public void addEventListener(final String type, final IEventListener listener, final boolean useCapture);
    public void addEventListener(final String type, final IEventListener listener);
    public void dispatchEvent(final Event event);
    public void dispatchEventWith(final String type, final boolean bubbles, final Object data);
    public void dispatchEventWith(final String type, final boolean bubbles);
    public void dispatchEventWith(final String type);
    public void removeEventListener(final String type, final IEventListener listener, final boolean useCapture);
    public void removeEventListener(final String type, final IEventListener listener);
}
