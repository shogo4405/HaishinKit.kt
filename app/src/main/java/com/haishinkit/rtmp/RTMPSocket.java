package com.haishinkit.rtmp;

import android.util.Log;

import java.nio.ByteBuffer;
import com.haishinkit.net.Socket;
import com.haishinkit.rtmp.message.RTMPMessage;

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

    public int getChunkSizeC() {
        return chunkSizeC;
    }

    public void setChunkSizeC(int chunkSizeC) {
        this.chunkSizeC = chunkSizeC;
    }

    public void doOutput(RTMPMessage message) {

    }

    @Override
    protected void onConnect() {
        Log.v(getClass().getName(), "onConnect");
        handshake.clear();
        readyState = ReadyState.VersionSent;
        doOutput(handshake.getC0C1Packet());
    }

    @Override
    protected void listen(ByteBuffer buffer) {
        Log.v(getClass().getName(), "readyState:" + readyState + ":" + buffer.toString());
        switch (readyState) {
            case VersionSent:
                if (buffer.limit() <= RTMPHandshake.SIGNAL_SIZE + 1) {
                    break;
                }
                handshake.setS0S1Packet(buffer);
                doOutput(handshake.getC2Packet());
                buffer.position(RTMPHandshake.SIGNAL_SIZE + 1);
                readyState = ReadyState.AckSent;
                if (buffer.limit() - buffer.position() == RTMPHandshake.SIGNAL_SIZE) {
                    listen(buffer.slice());
                }
                break;
            case AckSent:
                if (buffer.limit() <= RTMPHandshake.SIGNAL_SIZE) {
                    break;
                }
                handshake.setS2Packet(buffer);
                buffer.position(RTMPHandshake.SIGNAL_SIZE);
                readyState = ReadyState.HandshakeDone;
                break;
            case HandshakeDone:
                connection.listen(buffer);
                break;
            default:
                break;
        }
    }
}
