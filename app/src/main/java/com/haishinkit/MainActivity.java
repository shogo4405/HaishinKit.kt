package com.haishinkit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.haishinkit.rtmp.RTMPConnection;

public class MainActivity extends AppCompatActivity {

    private RTMPConnection connection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connection = new RTMPConnection();
        connection.connect("192.168.179.3");
    }
}
