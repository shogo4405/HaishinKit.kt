package com.haishinkit.rtmp;

import com.haishinkit.events.Event;
import com.haishinkit.events.EventDispatcher;
import com.haishinkit.events.IEventListener;
import com.haishinkit.lang.IRawValue;

import org.apache.commons.lang3.builder.ToStringBuilder;


public class RTMPStream extends EventDispatcher {

    class EventListener implements IEventListener {
        private final RTMPStream stream;

        EventListener(final RTMPStream stream) {
            this.stream = stream;
        }

        @Override
        public void handleEvent(final Event event) {
        }

        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    enum ReadyState implements IRawValue<Byte> {
        INITIALIZED((byte) 0x00),
        OPEN((byte) 0x01),
        PLAY((byte) 0x02),
        PLAYING((byte) 0x03),
        PUBLISH((byte) 0x04),
        PUBLISHING((byte) 0x05),
        CLOSED((byte) 0x06);

        private final byte rawValue;

        ReadyState(final byte rawValue) {
            this.rawValue = rawValue;
        }

        public Byte rawValue() {
            return rawValue;
        }
    }

    private double id = 0;
    private ReadyState readyState = ReadyState.INITIALIZED;
    private RTMPConnection connection = null;
    private final IEventListener listener = new EventListener(this);

    public RTMPStream(RTMPConnection connection) {
        super(null);
        this.connection = connection;
        this.connection.addEventListener(Event.RTMP_STATUS, listener);
        if (this.connection.isConnected()) {
            this.connection.createStream(this);
        }
    }

    double getId() {
        return id;
    }

    RTMPStream setId(final double id) {
        this.id = id;
        return this;
    }

    ReadyState getReadyState() {
        return readyState;
    }

    RTMPStream setReadyState(final ReadyState readyState) {
        this.readyState = readyState;
        return this;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
