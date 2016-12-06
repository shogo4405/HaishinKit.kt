package com.haishinkit.rtmp;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.AudioRecord;

import com.haishinkit.events.Event;
import com.haishinkit.events.EventDispatcher;
import com.haishinkit.events.IEventListener;
import com.haishinkit.lang.IRawValue;
import com.haishinkit.media.AACEncoder;
import com.haishinkit.media.AudioInfo;
import com.haishinkit.media.H264Encoder;
import com.haishinkit.media.IEncoder;
import com.haishinkit.rtmp.messages.RTMPCommandMessage;
import com.haishinkit.rtmp.messages.RTMPDataMessage;
import com.haishinkit.rtmp.messages.RTMPMessage;
import com.haishinkit.util.EventUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RTMPStream extends EventDispatcher {

    public enum HowToPublish implements IRawValue<String> {
        RECORD("record"),
        APPEND("append"),
        APPEND_WITH_GAP("appendWithGap"),
        LIVE("live");

        private final String valueOf;

        HowToPublish(final String valueOf) {
            this.valueOf = valueOf;
        }

        public final String rawValue() {
            return valueOf;
        }
    }

    public enum Codes implements IRawValue<String> {
        BUFFER_EMPTY("NetStream.Buffer.Empty", "status"),
        BUFFER_FLUSH("NetStream.Buffer.Flush", "status"),
        BUFFER_FULL("NetStream.Buffer.Full", "status");

        private final String rawValue;
        private final String level;

        Codes(final String rawValue, final String level) {
            this.rawValue = rawValue;
            this.level = level;
        }

        public Map<String, Object> data(final String description) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("code", rawValue);
            data.put("level", level);
            if (StringUtils.isNoneEmpty(description)) {
                data.put("description", description);
            }
            return data;
        }

        public final String getLevel() {
            return level;
        }

        public final String rawValue() {
            return rawValue;
        }
    }

    private final class EventListener implements IEventListener {
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
                case "NetStream.Publish.Start":
                    stream.setReadyState(ReadyState.PUBLISHING);
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

    RTMPConnection connection = null;
    private int id = 0;
    private RTMPMuxer muxer = null;
    private Camera camera = null;
    private AudioInfo audio = null;
    private short[] audioBuffer = null;
    private Map<String, IEncoder> encoders = new ConcurrentHashMap<String, IEncoder>();
    private ReadyState readyState = ReadyState.INITIALIZED;
    private List<RTMPMessage> messages = new ArrayList<RTMPMessage>();
    private final IEventListener listener = new EventListener(this);

    public RTMPStream(final RTMPConnection connection) {
        super(null);
        this.connection = connection;
        this.connection.addEventListener(Event.RTMP_STATUS, listener);
        if (this.connection.isConnected()) {
            this.connection.createStream(this);
        }
        addEventListener(Event.RTMP_STATUS, listener);
    }

    public void attachAudio(final AudioInfo audio) {
        if (audio == null) {
            return;
        }
        getEncoderByName("audio/mp4a-latm");
        final AudioRecord record = audio.getAudioRecord();
        final int frameBufferSize = audio.getMinBufferSize();
        record.setPositionNotificationPeriod(frameBufferSize / 2);
        record.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioRecord audioRecord) {
            }
            @Override
            public void onPeriodicNotification(AudioRecord audioRecord) {
                final byte[] buffer = audio.getBuffer();
                record.read(buffer, 0, frameBufferSize);
                getEncoderByName("audio/mp4a-latm").encodeBytes(buffer, System.nanoTime());
            }
        });
        record.startRecording();
        record.read(audio.getBuffer(), 0, frameBufferSize);
    }

    public void attachCamera(final Camera camera) {
        if (camera == null) {
            return;
        }
        getEncoderByName("video/avc");
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.YV12);
        parameters.setPreviewSize(320, 240);
        parameters.setPreviewFrameRate(30);
        camera.setParameters(parameters);
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
                getEncoderByName("video/avc").encodeBytes(bytes, System.nanoTime());
            }
        });
    }

    public final void publish(final String name) {
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
                .setChunkStreamID(RTMPChunk.AUDIO)
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
                setReadyState(ReadyState.PUBLISH);
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

    public void send(final String handlerName, final Object... arguments) {
        if (readyState == ReadyState.INITIALIZED || readyState == ReadyState.CLOSED) {
            return;
        }
        connection.getSocket().doOutput(RTMPChunk.ZERO,
                new RTMPDataMessage(connection.getObjectEncoding())
                    .setHandlerName(handlerName)
                    .setArguments(arguments == null ? null : Arrays.asList(arguments))
                    .setStreamID(getId())
                    .setChunkStreamID(RTMPChunk.COMMAND)
        );
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
            case PUBLISHING:
                send("@setDataFrame", "onMetaData", toMetaData());
                for (IEncoder encoder : encoders.values()) {
                    encoder.setListener(getMuxer());
                    encoder.startRunning();
                }
            default:
                break;
        }
        return this;
    }

    protected RTMPMuxer getMuxer() {
        if (muxer == null) {
            muxer = new RTMPMuxer(this);
        }
        return muxer;
    }

    protected Map<String, Object> toMetaData() {
        Map<String, Object> data = new HashMap<String, Object>();
        return data;
    }

    protected IEncoder getEncoderByName(final String mime) {
        if (!encoders.containsKey(mime)) {
            switch (mime) {
                case "video/avc":
                    encoders.put(mime, new H264Encoder());
                    break;
                case "audio/mp4a-latm":
                    encoders.put(mime, new AACEncoder());
                    break;
                default:
                    break;
            }
        }
        return encoders.get(mime);
    }
}
