package com.haishinkit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.haishinkit.rtmp.RTMPConnection;
import com.haishinkit.rtmp.RTMPStream;
import com.haishinkit.view.CameraView;

public class MainActivity extends AppCompatActivity {
    private CameraView cameraView = null;
    private RTMPConnection connection = null;
    private RTMPStream stream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connection = new RTMPConnection();
        stream = new RTMPStream(connection);

        cameraView = new CameraView(this);
        setContentView(cameraView);
        connection.connect("rtmp://192.168.179.3/live");
        //stream.attachCamera(cameraView.getCamera());
        stream.publish("live");
    }
}
