package com.haishinkit.rtmp;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.haishinkit.events.Event;
import com.haishinkit.events.EventDispatcher;
import com.haishinkit.events.IEventListener;
import com.haishinkit.lang.IRawValue;
import com.haishinkit.net.IResponder;
import com.haishinkit.rtmp.messages.RTMPCommandMessage;
import com.haishinkit.rtmp.messages.RTMPMessage;
import com.haishinkit.util.EventUtils;
import com.haishinkit.util.Log;

public class RTMPConnection extends EventDispatcher {
    public static final int DEFAULT_PORT = 1935;
    public static final String DEFAULT_FLASH_VER = "FMLE/3.0 (compatible; FMSc/1.0)";
    public static final RTMPObjectEncoding DEFAULT_OBJECT_ENCODING = RTMPObjectEncoding.AMF0;

    private static final int DEFAULT_CHUNK_SIZE = 1024 * 8;
    private static final int DEFAULT_CAPABILITIES = 239;

    public enum Codes implements IRawValue<String> {
        CALL_BAD_VERSION("NetConnection.Call.BadVersion", "error"),
        CALL_FAILED("NetConnection.Call.Failed", "error"),
        CALL_PROHIBITED("NetConnection.Call.Prohibited", "error"),
        CONNECT_APP_SHUTDOWN("NetConnection.Connect.AppShutdown", "status"),
        CONNECT_CLOSED("NetConnection.Connect.Closed", "status"),
        CONNECT_FAILED("NetConnection.Connect.Failed", "error"),
        CONNECT_IDLE_TIME_OUT("NetConnection.Connect.IdleTimeOut", "status"),
        CONNECT_INVALID_APP("NetConnection.Connect.InvalidApp", "error"),
        CONNECT_NETWORK_CHANGE("NetConnection.Connect.NetworkChange", "status"),
        CONNECT_REJECTED("NetConnection.Connect.Rejected", "status"),
        CONNECT_SUCCESS("NetConnection.Connect.Success", "status");

        private final String rawValue;
        private final String level;

        Codes(final String rawValue, final String level) {
            this.rawValue = rawValue;
            this.level = level;
        }

        public String getLevel() {
            return level;
        }

        public String rawValue() {
            return rawValue;
        }
    }

    public enum SupportSound implements IRawValue<Short> {
        NONE((short) 0x001),
        ADPCM((short) 0x002),
        MP3((short) 0x004),
        INTEL((short) 0x008),
        UNUSED((short) 0x0010),
        NELLY8((short) 0x0020),
        NELLY((short) 0x0040),
        G711A((short) 0x0080),
        G711U((short) 0x0100),
        AAC((short) 0x0200),
        SPEEX((short) 0x0800),
        ALL((short) 0x0FFF);

        private final short rawValue;

        SupportSound(final short rawValue) {
            this.rawValue = rawValue;
        }

        public Short rawValue() {
            return rawValue;
        }
    }

    public enum SupportVideo implements IRawValue<Short> {
        UNUSED((short) 0x001),
        JPEG((short) 0x002),
        SORENSON((short) 0x004),
        HOMEBREW((short) 0x008),
        VP6((short) 0x0010),
        VP6_ALPHA((short) 0x0020),
        HOMEBREWV((short) 0x0040),
        H264((short) 0x0080),
        ALL((short) 0x00FF);

        private final short rawValue;

        SupportVideo(final short rawValue) {
            this.rawValue = rawValue;
        }

        public Short rawValue() {
            return this.rawValue;
        }
    }

    public enum VideoFunction implements IRawValue<Short> {
        CLIENT_SEEK((short) 1);

        private final short rawValue;

        VideoFunction(final short rawValue) {
            this.rawValue = rawValue;
        }

        public Short rawValue() {
            return this.rawValue;
        }
    }

    private URI uri = null;
    private String swfUrl = null;
    private String pageUrl = null;
    private String flashVer = RTMPConnection.DEFAULT_FLASH_VER;
    private RTMPObjectEncoding objectEncoding = RTMPConnection.DEFAULT_OBJECT_ENCODING;
    private int transactionID = 0;
    private Object[] arguments = null;
    private Map<Short, ByteBuffer> messages = new HashMap<Short, ByteBuffer>();
    private Map<Integer, IResponder> responders = new ConcurrentHashMap<Integer, IResponder>();
    private RTMPSocket socket = new RTMPSocket(this);

    public RTMPConnection() {
        super(null);
    }

    public RTMPSocket getSocket() {
        return socket;
    }

    public URI getUri() {
        return uri;
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public String getSwfUrl() {
        return swfUrl;
    }

    public RTMPConnection setSwfUrl(String swfUrl) {
        this.swfUrl = swfUrl;
        return this;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public RTMPConnection setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
        return this;
    }

    public String getFlashVer() {
        return this.flashVer;
    }

    public RTMPConnection setFlashVer(String flashVer) {
        this.flashVer = flashVer;
        return this;
    }

    public Map<Integer, IResponder>  getResponders() {
        return responders;
    }

    public void call(final String commandName, final IResponder responder, final Object... arguments) {
        if (!isConnected()) {
            return;
        }
        List<Object> listArguments = new ArrayList<Object>(arguments.length);
        for (Object object : arguments) {
            listArguments.add(object);
        }
        RTMPCommandMessage message = new RTMPCommandMessage(objectEncoding);
        message.setStreamID(0);
        message.setTransactionID(++transactionID);
        message.setCommandName(commandName);
        message.setArguments(listArguments);
        if (responder != null) {
            getResponders().put(transactionID, responder);
        }
        getSocket().doOutput(RTMPChunk.ZERO, message);
    }

    public void call(final String commandName, final IResponder responder) {
        call(commandName, responder);
    }

    public void call(final String commandName) {
        call(commandName, null);
    }

    public void connect(final String command, final Object... arguments) {
        uri = URI.create(command);
        if (isConnected() || !uri.getScheme().equals("rtmp")) {
            return;
        }
        int port = uri.getPort();
        this.arguments = arguments;
        socket.connect(uri.getHost(), port == -1 ? RTMPConnection.DEFAULT_PORT : port);
    }

    public void close() {
        if (!isConnected()) {
            return;
        }
        socket.close();
    }

    void listen(final ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            return;
        }
        byte first = buffer.get();
        RTMPChunk chunk = RTMPChunk.rawValue((byte) (first >> 6));
        buffer.position(buffer.position() - 1);
        short streamID = chunk.getStreamID(buffer);
        if (messages.containsKey(streamID)) {

        } else {
            chunk.decode(socket, buffer).execute(this);
            listen(buffer);
        }
    }

    void createStream(final RTMPStream stream) {
        call("commandName", new IResponder() {
            @Override
            public void onResult(List<Object> arguments) {
                stream.setReadyState(RTMPStream.ReadyState.OPEN);
            }
            @Override
            public void onStatus(List<Object> arguments) {
                Log.w(getClass().getName() + "#onStatus", "");
            }
        });
    }

    RTMPMessage createConnectionMessage() {
        String[] paths = uri.getPath().split("/", 0);
        RTMPCommandMessage message = new RTMPCommandMessage(RTMPObjectEncoding.AMF0);
        Map<String, Object> commandObject = new HashMap<String, Object>();
        commandObject.put("app", paths[1]);
        commandObject.put("flashVer", getFlashVer());
        commandObject.put("swfUrl", getSwfUrl());
        commandObject.put("tcUrl", uri.toString());
        commandObject.put("fpad", false);
        commandObject.put("capabilities", RTMPConnection.DEFAULT_CAPABILITIES);
        commandObject.put("audioCodecs", SupportSound.AAC.rawValue());
        commandObject.put("videoCodecs", SupportVideo.H264.rawValue());
        commandObject.put("videoFunction", VideoFunction.CLIENT_SEEK.rawValue());
        commandObject.put("pageUrl", getPageUrl());
        commandObject.put("objectEncoding", objectEncoding.rawValue());
        message.setChunkStreamID(RTMPChunk.COMMAND);
        message.setStreamID(0);
        message.setCommandName("connect");
        message.setTransactionID(++transactionID);
        message.setCommandObject(commandObject);
        if (arguments != null) {
            List<Object> args = new ArrayList<Object>(arguments.length);
            for (Object object : arguments) {
                args.add(object);
            }
            message.setArguments(args);
        }
        return message;
    }
}
