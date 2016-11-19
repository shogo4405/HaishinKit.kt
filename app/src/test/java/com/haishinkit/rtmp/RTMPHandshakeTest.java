package com.haishinkit.rtmp;

import org.junit.Test;
import java.nio.ByteBuffer;
import static org.junit.Assert.*;

public class RTMPHandshakeTest {
    @Test
    public void main() throws Exception {
        RTMPHandshake handshake = new RTMPHandshake();
        ByteBuffer C0C1Packet = handshake.getC0C1Packet();
        handshake.setS0S1Packet(C0C1Packet);
    }
}
