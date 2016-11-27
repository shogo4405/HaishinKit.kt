package com.haishinkit.rtmp;

import com.haishinkit.events.EventDispatcher;

public class RTMPStream extends EventDispatcher {
    private RTMPConnection connection = null;

    public RTMPStream(RTMPConnection connection) {
        super(null);
        this.connection = connection;
    }
}
