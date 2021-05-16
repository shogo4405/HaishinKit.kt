# HaishinKit.kt
[![GitHub license](https://img.shields.io/badge/license-New%20BSD-blue.svg)](https://raw.githubusercontent.com/shogo4405/HaishinKit.kt/master/LICENSE.md)

* Technical Preview. Camera and Microphone streaming library via RTMP for Android.
* [API Documentation](https://shogo4405.github.io/HaishinKit.kt/haishinkit/)
* Issuesの言語は、日本語が分かる方は日本語でお願いします！

## Communication
* If you need help with making LiveStreaming requests using HaishinKit, use a GitHub issue with **Bug report template**
  - If you don't use an issue template. I will immediately close the your issue without a comment.
* If you'd like to discuss a feature request, use a GitHub issue with **Feature request template**.
* If you want to support e-mail based communication without GitHub issue.
  - Consulting fee is [$50](https://www.paypal.me/shogo4405/50USD)/1 incident. I'm able to response a few days.
* If you **want to contribute**, submit a pull request!

## Features
### RTMP
- [ ] Authentication
- [x] Publish (H264/AAC) 
- [x] Playback
- [ ] Action Message Format
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
BSD-3-Clause

## Donation
Paypal
 - https://www.paypal.me/shogo4405

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
class CameraTabFragment: Fragment(), IEventListener {
    private lateinit var connection: RtmpConnection
    private lateinit var stream: RtmpStream
    private lateinit var cameraView: GlHkView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 1)
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
        connection = RtmpConnection()
        stream = RtmpStream(connection)
        stream.attachAudio(AudioRecordSource())

        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val camera = CameraSource(activity).apply {
            this.open(cameraId)
        }
        stream.attachVideo(camera)
        connection.addEventListener(Event.RTMP_STATUS, this)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_camera, container, false)
        val button = v.findViewById<Button>(R.id.button)
        button.setOnClickListener {
            if (button.text == "Publish") {
                connection.connect(Preference.shared.rtmpURL)
                button.text = "Stop"
            } else {
                connection.close()
                button.text = "Publish"
            }
        }
        cameraView = v.findViewById<GlHkView>(R.id.camera)
        cameraView.attachStream(stream)
        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.dispose()
    }

    override fun handleEvent(event: Event) {
        Log.i(javaClass.name + "#handleEvent", event.toString())
        val data = EventUtils.toMap(event)
        val code = data["code"].toString()
        if (code == RtmpConnection.Code.CONNECT_SUCCESS.rawValue) {
            stream.publish(Preference.shared.streamName)
        }
    }

    companion object {
        fun newInstance(): CameraTabFragment {
            return CameraTabFragment()
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
