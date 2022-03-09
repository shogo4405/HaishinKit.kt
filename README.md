# HaishinKit.kt
[![GitHub license](https://img.shields.io/badge/license-New%20BSD-blue.svg)](https://raw.githubusercontent.com/shogo4405/HaishinKit.kt/master/LICENSE.md)

* Camera and Microphone streaming library via RTMP for Android.
* [API Documentation](https://shogo4405.github.io/HaishinKit.kt/)
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
- [x] Authentication
- [x] Publish (H264/AAC) 
- [x] Playback
- [ ] Action Message Format
  - [x] AMF0
  - [ ] ~~AMF3~~
- [ ] ~~SharedObject~~
- [x] RTMPS
  - [x] Native (RTMP over SSL/TSL)

### Filter
- [x] Monochrome

### Sources
- [x] Camera with Camera2 api
- [x] MediaProjection
- [x] Microphone with AudioRecord api.

### View rendering
|-|HkSurfaceView|HkTextureView|
|:-|:-:|:-:|
|Engine|SurfaceView|TextureView|
|Playback|beta|beta|
|Publish|✅ Stable|✅ Stable|
|Note|Recommend Android 7.0+|Recommend Android 5.0-6.0|

### Others
- [x] Hardware acceleration for H264 video encoding/AAC audio encoding.
  - [x] Asynchronously processing.
- [ ] Graphics api
  - [x] OpenGL
  - [ ] Vulkan

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

## Requirements
|-|Android|
|:----:|:----:|
|0.7.0|5.0+|

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
    private lateinit var cameraView: HkGLSurfaceView
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            val permissionCheck = ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.CAMERA), 1)
            }
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            }
        }
        connection = RtmpConnection()
        stream = RtmpStream(connection)
        stream.attachAudio(AudioRecordSource())
        cameraSource = CameraSource(requireContext()).apply {
            open(CameraCharacteristics.LENS_FACING_BACK)
        }
        stream.attachVideo(cameraSource)
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
        val switchButton = v.findViewById<Button>(R.id.switch_button)
        switchButton.setOnClickListener {
            cameraSource.switchCamera()
        }
        cameraView = v.findViewById(R.id.camera)
        cameraView.attachStream(stream)
        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.dispose()
    }

    override fun handleEvent(event: Event) {
        Log.i("$TAG#handleEvent", event.toString())
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

        private val TAG = CameraTabFragment::class.java.simpleName
    }
}
```

### Filter API (v0.1)
```
- [assets]
  - [shaders]
    - custom-shader.vert(optional)
    - custom-shader.frag
```

```
package my.custom.filter

import com.haishinkit.graphics.filter.VideoEffect

class Monochrome2VideoEffect(
    override val name: String = "custom-shader"
) : VideoEffect
```

```
stream.videoEffect = Monochrome2VideoEffect()
```

## FAQ
### How can I run example project?
```sh
git clone https://github.com/shogo4405/HaishinKit.kt.git
cd HaishinKit.kt
git submodule update --init

# Open [Android Studio] -> [Open] ...
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
