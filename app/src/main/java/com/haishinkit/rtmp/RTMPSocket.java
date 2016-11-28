package com.haishinkit.rtmp;

import java.nio.ByteBuffer;
import com.haishinkit.net.Socket;
import com.haishinkit.util.Log;
import com.haishinkit.rtmp.messages.RTMPMessage;

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

    private int bandwidth = 0;
    private int chunkSizeC = RTMPChunk.DEFAULT_SIZE;
    private int chunkSizeS = RTMPChunk.DEFAULT_SIZE;
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

    public int getChunkSizeS() {
        return chunkSizeS;
    }

    public RTMPSocket setChunkSizeS(final int chunkSizeS) {
        this.chunkSizeS = chunkSizeS;
        return this;
    }

    public int getChunkSizeC() {
        return chunkSizeC;
    }

    public RTMPSocket setChunkSizeC(final int chunkSizeC) {
        this.chunkSizeC = chunkSizeC;
        return this;
    }

    public void doOutput(final RTMPChunk chunk, final RTMPMessage message) {
        if (chunk == null || message == null) {
            throw new IllegalArgumentException();
        }
        for (ByteBuffer buffer : chunk.encode(this, message)) {
            doOutput(buffer);
        }
    }

    @Override
    protected void onConnect() {
        Log.v(getClass().getName() + "#onConnect", "");
        chunkSizeC = RTMPChunk.DEFAULT_SIZE;
        chunkSizeS = RTMPChunk.DEFAULT_SIZE;
        handshake.clear();
        readyState = ReadyState.VersionSent;
        doOutput(handshake.getC0C1Packet());
    }

    @Override
    protected void listen(final ByteBuffer buffer) {
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
                connected = true;
                doOutput(RTMPChunk.ZERO, connection.createConnectionMessage());
                break;
            case HandshakeDone:
                try {
                    connection.listen(buffer);
                } catch (IndexOutOfBoundsException e) {

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    throw e;
                }
                break;
            default:
                break;
        }
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
