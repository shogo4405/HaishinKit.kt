package com.haishinkit.rtmp;

import com.haishinkit.util.Log;

import org.junit.Test;

public final class RTMPConnectionTests {
    @Test
    public void connect() {
        RTMPConnection connection = new RTMPConnection();
        connection.connect("rtmp://localhost/live");
        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            Log.i("RTMPConnection#connect", e.toString());
        }
    }
}
