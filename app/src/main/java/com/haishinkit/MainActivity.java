package com.haishinkit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Camera;

import com.haishinkit.media.AudioInfo;
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
        stream.attachAudio(new AudioInfo());

        Camera camera = cameraView.getCamera();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(320, 240);
        parameters.setPreviewFrameRate(30);
        camera.setParameters(parameters);

        //stream.attachCamera(cameraView.getCamera());
        setContentView(cameraView);

        connection.connect("rtmp://192.168.179.3/live");
        stream.publish("live");
    }
}
