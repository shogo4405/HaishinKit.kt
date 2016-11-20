package com.haishinkit.rtmp;

import android.util.Log;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import com.haishinkit.rtmp.message.RTMPCommandMessage;
import com.haishinkit.rtmp.message.RTMPMessage;

public class RTMPConnection {
    public static final int DEFAULT_PORT = 1935;
    public static final String DEFAULT_FLASH_VER = "FMLE/3.0 (compatible; FMSc/1.0)";
    public static final RTMPObjectEncoding DEFAULT_OBJECT_ENCODING = RTMPObjectEncoding.AMF0;

    private static final int DEFAULT_CHUNK_SIZE = 1024 * 8;
    private static final int DEFAULT_CAPABILITIES = 239;

    enum SupportSound {
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

        private final short value;

        SupportSound(short value) {
            this.value = value;
        }

        short valueOf() {
            return this.value;
        }
    }

    enum SupportVideo {
        UNUSED((short) 0x001),
        JPEG((short) 0x002),
        SORENSON((short) 0x004),
        HOMEBREW((short) 0x008),
        VP6((short) 0x0010),
        VP6_ALPHA((short) 0x0020),
        HOMEBREWV((short) 0x0040),
        H264((short) 0x0080),
        ALL((short) 0x00FF);

        private final short value;

        SupportVideo(short value) {
            this.value = value;
        }

        short valueOf() {
            return this.value;
        }
    }

    enum VideoFunction {
        CLIENT_SEEK((short) 1);

        private final short value;

        VideoFunction(short value) {
            this.value = value;
        }

        short valueOf() {
            return this.value;
        }
    }

    private URI uri = null;
    private String swfUrl = null;
    private String pageUrl = null;
    private String flashVer = RTMPConnection.DEFAULT_FLASH_VER;
    private RTMPObjectEncoding objectEncoding = RTMPConnection.DEFAULT_OBJECT_ENCODING;
    private int transactionID = 0;
    private Object[] arguments = null;
    private RTMPSocket socket = new RTMPSocket(this);

    public RTMPConnection() {
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

    public void setSwfUrl(String swfUrl) {
        this.swfUrl = swfUrl;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getFlashVer() {
        return this.flashVer;
    }

    public void setFlashVer(String flashVer) {
        this.flashVer = flashVer;
    }

    public void connect(final String command, Object... arguments) {
        if (isConnected()) {
            return;
        }
        uri = URI.create(command);
        this.arguments = arguments;
        socket.connect(command, 1935);
    }

    public void close() {
        if (!isConnected()) {
            return;
        }
        socket.close();
    }

    void listen(ByteBuffer buffer) {
        Log.e(getClass().getName(), buffer.toString());
    }

    RTMPMessage createConnectionMessage() {
        RTMPCommandMessage message = new RTMPCommandMessage(RTMPObjectEncoding.AMF0);
        Map<String, Object> commandObject = new HashMap<String, Object>();
        commandObject.put("app", "");
        commandObject.put("flashVer", flashVer);
        commandObject.put("swfUrl", swfUrl);
        commandObject.put("tcUrl", uri.toString());
        commandObject.put("fpad", false);
        commandObject.put("capabilities", RTMPConnection.DEFAULT_CAPABILITIES);
        commandObject.put("audioCodecs", SupportSound.AAC.valueOf());
        commandObject.put("videoCodecs", SupportVideo.H264.valueOf());
        commandObject.put("videoFunction", VideoFunction.CLIENT_SEEK.valueOf());
        commandObject.put("pageUrl", pageUrl);
        commandObject.put("objectEncoding", objectEncoding.valueOf());
        message.setCommandName("connect");
        message.setTransactionID(transactionID++);
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
