package com.haishinkit.rtmp.messages;

import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPSocket;

import java.nio.ByteBuffer;

public final class RTMPAudioMessage extends RTMPMessage {
    public RTMPAudioMessage() {
        super(Type.AUDIO);
    }

    @Override
    public RTMPMessage decode(final RTMPSocket socket, final ByteBuffer buffer) {
        buffer.position(buffer.position() + getLength());
        return this;
    }

    @Override
    public RTMPMessage execute(final RTMPConnection connection) {
        return this;
    }
}
