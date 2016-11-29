package com.haishinkit.rtmp;

import android.graphics.ImageFormat;
import android.hardware.Camera;

import com.haishinkit.events.Event;
import com.haishinkit.events.EventDispatcher;
import com.haishinkit.events.IEventListener;
import com.haishinkit.lang.IRawValue;
import com.haishinkit.media.H264Encoder;
import com.haishinkit.media.IEncoderDelegate;
import com.haishinkit.rtmp.messages.RTMPCommandMessage;
import com.haishinkit.rtmp.messages.RTMPMessage;
import com.haishinkit.util.EventUtils;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    public enum HowToPublish implements IRawValue<String> {
        RECORD("record"),
        APPEND("append"),
        APPEND_WITH_GAP("appendWithGap"),
        LIVE("live");

        private final String valueOf;

        HowToPublish(final String valueOf) {
            this.valueOf = valueOf;
        }

        public String rawValue() {
            return valueOf;
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
    private H264Encoder encoder = new H264Encoder();
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

    public void attachCamera(final Camera camera) {
        if (camera == null) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewSize(320, 240);
        camera.setParameters(parameters);
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
                encoder.encodeBytes(bytes);
            }
        });
        encoder.startRunning();
    }

    public void publish(final String name) {
        publish(name, HowToPublish.LIVE);
    }

    public void publish(final String name, final HowToPublish howToPublish) {

        List<Object> arguments = null;
        if (name != null) {
            arguments = new ArrayList<Object>(2);
            arguments.add(name);
            arguments.add(howToPublish.rawValue());
        }

        RTMPMessage message = new RTMPCommandMessage(connection.getObjectEncoding())
                .setTransactionID(0)
                .setCommandName(name != null ? "publish" : "closeStream")
                .setArguments(arguments)
                .setChunkStreamID(RTMPChunk.CONTROL)
                .setStreamID(getId());

        if (name == null) {
            switch (readyState) {
                case PUBLISHING:
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
                connection.getSocket().doOutput(RTMPChunk.ZERO, message);
                break;
            default:
                break;
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
