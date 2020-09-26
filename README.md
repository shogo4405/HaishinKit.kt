# HaishinKit.kt
[![GitHub license](https://img.shields.io/badge/license-New%20BSD-blue.svg)](https://raw.githubusercontent.com/shogo4405/HaishinKit.kt/master/LICENSE.md)

Technical Preview. Camera and Microphone streaming library via RTMP for Android.
* [API Documentation](https://shogo4405.github.io/HaishinKit.kt/haishinkit/)

## Features
### RTMP
- [ ] Authentication
- [x] Publish (H264/AAC) 
- [ ] ~Playback~
- [x] AMF0
- [ ] AMF3
- [ ] SharedObject
- [ ] RTMPS
  - [ ] Native (RTMP over SSL/TSL)
  - [ ] Tunneled (RTMPT over SSL/TSL)

### Others
- [x] Hardware acceleration for H264 video encoding/AAC audio encoding.
  - [x] Asynchronously processing.

## License
New BSD

## Donation
Bitcoin
```txt
3FnjC3CmwFLTzNY5WPNz4LjTo1uxGNozUR
```

## Android manifest
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## Prerequisites
```kt
ActivityCompat.requestPermissions(this,arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO
), 1)
```

## RTMP Usage
Real Time Messaging Protocol (RTMP).

```kt
class MainActivity : AppCompatActivity(), IEventListener {

    private var connection: RTMPConnection? = null
    private var stream: RTMPStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO),
                1)

        setContentView(R.layout.activity_main)

        connection = RTMPConnection()
        stream = RTMPStream(connection!!)
        stream?.attachCamera(Camera(android.hardware.Camera.open()))
        stream?.attachAudio(Audio())
        connection?.addEventListener("rtmpStatus", this)

        val button = findViewById(R.id.button) as Button
        button.setOnClickListener {
            connection?.connect("rtmp://192.168.11.15/live")
        }
    }

    override fun onStart() {
        super.onStart()
        val view1 = findViewById(R.id.camera) as CameraView
        view1.attachStream(stream!!)
    }

    override fun handleEvent(event: Event) {
        val data = EventUtils.toMap(event)
        val code = data["code"].toString()
        if (code == RTMPConnection.Codes.CONNECT_SUCCESS.rawValue) {
            Log.w(javaClass.name, "PUBLISH")
            stream!!.publish("live")
        }
    }
}
```
### RTML URL Format
* rtmp://server-ip-address[:port]/application/[appInstance]/[prefix:[path1[/path2/]]]streamName
  - [] mark is an Optional.
  ```
  rtmpConneciton.connect("rtmp://server-ip-address[:port]/application/[appInstance]")
  rtmpStream.publish("[prefix:[path1[/path2/]]]streamName")
  ```
* rtmp://localhost/live/streamName
  ```
  rtmpConneciton.connect("rtmp://localhost/live")
  rtmpStream.publish("streamName")
  ```

## Related Project
* HaishinKit.swift - Camera and Microphone streaming library via RTMP, HLS for iOS, macOS and tvOS.
  * https://github.com/shogo4405/HaishinKit.swift
