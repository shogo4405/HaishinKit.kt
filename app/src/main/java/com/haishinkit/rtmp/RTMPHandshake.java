package com.haishinkit.rtmp;

import android.util.Log;

import com.haishinkit.util.ByteBufferUtils;

import java.nio.ByteBuffer;
import java.util.Random;

public final class RTMPHandshake {
    public static int SIGNAL_SIZE = 1536;

    private ByteBuffer C0C1Packet = null;
    private ByteBuffer S0S1Packet = null;
    private ByteBuffer C2Packet = null;
    private ByteBuffer S2Packet = null;

    public RTMPHandshake() {
    }

    public ByteBuffer getC0C1Packet() {
        if (C0C1Packet == null) {
            Random random = new Random();
            C0C1Packet = ByteBuffer.allocate(SIGNAL_SIZE + 1);
            C0C1Packet.put((byte) 0x03);
            C0C1Packet.position(1 + 8);
            for(int i = 0;i < SIGNAL_SIZE - 8;++i) {
                C0C1Packet.put((byte) random.nextInt(16));
            }
            C0C1Packet.flip();
        }
        return C0C1Packet;
    }

    public void setC0C1Packet(ByteBuffer C0C1Packet) {
        this.C0C1Packet = C0C1Packet;
    }

    public ByteBuffer getS0S1Packet() {
        return S0S1Packet;
    }

    public void setS0S1Packet(ByteBuffer S0S1Packet) {
        this.S0S1Packet = ByteBuffer.wrap(S0S1Packet.array(), 0, SIGNAL_SIZE + 1);
        C2Packet = ByteBuffer.allocate(SIGNAL_SIZE);
        C2Packet.put(S0S1Packet.array(), 1, 4);
        C2Packet.position(8);
        C2Packet.put(S0S1Packet.array(), 9, SIGNAL_SIZE - 8);
    }

    public ByteBuffer getC2Packet() {
        return C2Packet;
    }

    public void setC2Packet(ByteBuffer C2Packet) {
        this.C2Packet = C2Packet;
    }

    public ByteBuffer getS2Packet() {
        return S2Packet;
    }

    public void setS2Packet(ByteBuffer S2Packet) {
        this.S2Packet = ByteBuffer.wrap(S2Packet.array(), 0, SIGNAL_SIZE);;
    }

    public void clear() {
        C0C1Packet = null;
        S0S1Packet = null;
        C2Packet = null;
        S2Packet = null;
    }
}
