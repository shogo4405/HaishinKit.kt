package com.haishinkit.rtmp;

import java.nio.ByteBuffer;
import com.haishinkit.net.Socket;
import com.haishinkit.util.Log;
import com.haishinkit.rtmp.message.RTMPMessage;

import org.apache.commons.lang3.builder.ToStringBuilder;

public final class RTMPSocket extends Socket {
    enum ReadyState {
        Uninitialized,
        VersionSent,
        AckSent,
        HandshakeDone,
        Closing,
        Closed
    }

    private int chunkSizeC = RTMPChunk.DEFAULT_SIZE;
    private int bandwidth = 0;
    private boolean connected = false;
    private RTMPHandshake handshake = new RTMPHandshake();
    private ReadyState readyState = ReadyState.Uninitialized;
    private RTMPConnection connection = null;

    public RTMPSocket(final RTMPConnection connection) {
        this.connection = connection;
    }

    public boolean isConnected() {
        return connected;
    }

    public int getBandWidth() {
        return bandwidth;
    }

    public RTMPSocket setBandwidth(final int bandwidth) {
        this.bandwidth = bandwidth;
        return this;
    }

    public int getChunkSizeC() {
        return chunkSizeC;
    }

    public void setChunkSizeC(int chunkSizeC) {
        this.chunkSizeC = chunkSizeC;
    }

    public void doOutput(RTMPChunk chunk, RTMPMessage message) {
        for (ByteBuffer buffer : chunk.encode(this, message)) {
            doOutput(buffer);
        }
    }

    @Override
    protected void onConnect() {
        Log.v(getClass().getName() + "#onConnect", "");
        handshake.clear();
        readyState = ReadyState.VersionSent;
        doOutput(handshake.getC0C1Packet());
    }

    @Override
    protected void listen(ByteBuffer buffer) {
        Log.v(getClass().getName() + "#listen", "readyState:" + readyState + ":" + buffer.toString());
        switch (readyState) {
            case VersionSent:
                if (buffer.limit() < RTMPHandshake.SIGNAL_SIZE + 1) {
                    break;
                }
                handshake.setS0S1Packet(buffer);
                doOutput(handshake.getC2Packet());
                buffer.position(RTMPHandshake.SIGNAL_SIZE + 1);
                readyState = ReadyState.AckSent;
                if (buffer.limit() - buffer.position() == RTMPHandshake.SIGNAL_SIZE) {
                    listen(buffer.slice());
                    buffer.position(3073);
                }
                break;
            case AckSent:
                if (buffer.limit() < RTMPHandshake.SIGNAL_SIZE) {
                    break;
                }
                handshake.setS2Packet(buffer);
                buffer.position(RTMPHandshake.SIGNAL_SIZE);
                readyState = ReadyState.HandshakeDone;
                doOutput(RTMPChunk.ZERO, connection.createConnectionMessage());
                break;
            case HandshakeDone:
                connection.listen(buffer);
                break;
            default:
                break;
        }
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
