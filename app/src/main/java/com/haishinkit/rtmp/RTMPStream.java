package com.haishinkit.rtmp;

import com.haishinkit.events.Event;
import com.haishinkit.events.EventDispatcher;
import com.haishinkit.events.IEventListener;
import com.haishinkit.lang.IRawValue;
import com.haishinkit.rtmp.messages.RTMPCommandMessage;
import com.haishinkit.rtmp.messages.RTMPMessage;
import com.haishinkit.util.EventUtils;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.List;

public class RTMPStream extends EventDispatcher {

    private class EventListener implements IEventListener {
        private final RTMPStream stream;

        EventListener(final RTMPStream stream) {
            this.stream = stream;
        }

        @Override
        public void handleEvent(final Event event) {
            Map<String, Object> data = EventUtils.toMap(event);
            switch (data.get("code").toString()) {
                case "NetConnection.Connect.Success":
                    connection.createStream(stream);
                    break;
                default:
                    break;
            }
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

    private int id = 0;
    private ReadyState readyState = ReadyState.INITIALIZED;
    private RTMPConnection connection = null;
    private List<RTMPMessage> messages = new ArrayList<RTMPMessage>();
    private final IEventListener listener = new EventListener(this);

    public RTMPStream(final RTMPConnection connection) {
        super(null);
        this.connection = connection;
        this.connection.addEventListener(Event.RTMP_STATUS, listener);
        if (this.connection.isConnected()) {
            this.connection.createStream(this);
        }
    }

    public void play(final Object... arguments) {
        if (arguments == null) {
            throw new IllegalArgumentException();
        }

        Object streamName = arguments[0];
        RTMPMessage message = new RTMPCommandMessage(connection.getObjectEncoding())
                .setTransactionID(0)
                .setCommandName(streamName != null ? "play" : "closeStream")
                .setArguments(Arrays.asList(arguments))
                .setChunkStreamID(RTMPChunk.CONTROL)
                .setStreamID(getId());

        if (streamName == null) {
            switch (readyState) {
                case PLAYING:
                    connection.getSocket().doOutput(RTMPChunk.ZERO, message);
                    break;
                default:
                    break;
            }
            return;
        }

        switch (readyState) {
            case INITIALIZED:
                messages.add(message);
                break;
            case OPEN:
            case PLAYING:
                connection.getSocket().doOutput(RTMPChunk.ZERO, message);
                break;
            default:
                break;
        }
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    int getId() {
        return id;
    }

    RTMPStream setId(final int id) {
        this.id = id;
        return this;
    }

    ReadyState getReadyState() {
        return readyState;
    }

    RTMPStream setReadyState(final ReadyState readyState) {
        this.readyState = readyState;
        switch (readyState) {
            case OPEN:
                for (RTMPMessage message : messages) {
                    message.setStreamID(getId());
                    connection.getSocket().doOutput(RTMPChunk.ZERO, message);
                }
                messages.clear();
                break;
            default:
                break;
        }
        return this;
    }
}
