# HaishinKit for Android, [iOS, macOS and tvOS](https://github.com/shogo4405/HaishinKit.swift).
[![GitHub license](https://img.shields.io/badge/license-New%20BSD-blue.svg)](https://raw.githubusercontent.com/shogo4405/HaishinKit.kt/master/LICENSE.md)
[![](https://jitpack.io/v/shogo4405/HaishinKit~kt.svg)](https://jitpack.io/#shogo4405/HaishinKit~kt)

* Camera and Microphone streaming library via RTMP for Android.
* [API Documentation](https://shogo4405.github.io/HaishinKit.kt/)

## 💬 Communication
* If you need help with making LiveStreaming requests using HaishinKit, use a [GitHub Discussions](https://github.com/shogo4405/HaishinKit.kt/discussions) with **Q&A**.
* If you'd like to discuss a feature request, use a [GitHub Discussions](https://github.com/shogo4405/HaishinKit.kt/discussions) with **Idea**
* If you met a HaishinKit's bug🐛, use a [GitHub Issue](https://github.com/shogo4405/HaishinKit.kt/issues) with **Bug report template**
  - If you don't use an issue template. I will immediately close the your issue without a comment.
* If you **want to contribute**, submit a pull request!
* If you want to support e-mail based communication without GitHub.
  - Consulting fee is [$50](https://www.paypal.me/shogo4405/50USD)/1 incident. I'm able to response a few days.
* [Discord chatroom](https://discord.com/invite/8nkshPnanr).
* 日本語が分かる方は日本語でお願いします！

## 🌏 Related projects
Project name    |Notes       |License
----------------|------------|--------------
[HaishinKit for iOS, macOS and tvOS.](https://github.com/shogo4405/HaishinKit.swift)|Camera and Microphone streaming library via RTMP for Android.|[BSD 3-Clause "New" or "Revised" License](https://github.com/shogo4405/HaishinKit.swift/blob/master/LICENSE.md)
[HaishinKit for Flutter.](https://github.com/shogo4405/HaishinKit.dart)|Camera and Microphone streaming library via RTMP for Flutter.|[BSD 3-Clause "New" or "Revised" License](https://github.com/shogo4405/HaishinKit.dart/blob/master/LICENSE.md)

## 🎨 Features
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
- [Table Of Filters](https://shogo4405.github.io/HaishinKit.kt/haishinkit/com.haishinkit.graphics.effect/index.html)

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
- [x] Graphics api
  - [x] ✅ OpenGL
  - [x] 🐛 Vulkan (beta)

### Settings
```
stream.audioSettings.bitrate = 32 * 1000

stream.videoSettings.width = 640 // The width resoulution of video output.
stream.videoSettings.height = 360 // The height resoulution of video output.
stream.videoSettings.bitrate = 160 * 1000 // The bitRate of video output.
stream.videoSettings.IFrameInterval = 2 // The key-frmae interval
```

### Offscreen Rendering.
Through off-screen rendering capabilities, it is possible to display any text or bitmap on a video during broadcasting or viewing. This allows for various applications such as watermarking and time display.

<p align="center">
  <img width="732" alt="" src="https://github.com/shogo4405/HaishinKit.kt/assets/810189/f2e189eb-d98a-41b4-9b4c-0b7d70637675">
</p>

```
stream.attachVideo(cameraSource)

val text = Text()
text.textSize = 60f
text.textValue = "23:44:56"
text.layoutMargins.set(0, 0, 16, 16)
text.horizontalAlignment = ScreenObject.HORIZONTAL_ALIGNMENT_RIGHT
text.verticalAlignment = ScreenObject.VERTICAL_ALIGNMENT_BOTTOM
stream.screen.addChild(text)

val image = Image()
image.bitmap = BitmapFactory.decodeResource(resources, R.drawable.game_jikkyou)
image.verticalAlignment = ScreenObject.VERTICAL_ALIGNMENT_BOTTOM
image.frame.set(0, 0, 180, 180)
stream.screen.addChild(image)
```

## 🌏 Architecture Overview
### Publishing Feature
<p align="center">
  <img width="732" alt="" src="https://user-images.githubusercontent.com/810189/164874912-3cdc0dde-2cfb-4c94-9404-eeb2ff6091ac.png">
</p>

## 🐾 Examples
Examples project are available for Android.
- [x] Camera and microphone publish.
- [x] RTMP Playback  
```sh
git clone https://github.com/shogo4405/HaishinKit.kt.git
cd HaishinKit.kt
git submodule update --init

# Open [Android Studio] -> [Open] ...
```

## 🔧 Usage

### Gradle dependency
**JitPack**
```
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}

dependencies {
  implementation 'com.github.shogo4405.HaishinKit~kt:haishinkit:x.x.x'
  implementation 'com.github.shogo4405.HaishinKit~kt:lottie:x.x.x'
  implementation 'com.github.shogo4405.HaishinKit~kt:vulkan:x.x.x'
}
```

### Dependencies
|-|minSdk|Android|Requirements|Status|Description|
|:----|:----|:----|:-----|:----|:----|
|haishinkit|21+|5|Require|Stable|It's the base module for HaishinKit.|
|lottie|21+|5|Optional|Beta|It's a module for embedding Lottie animations into live streaming video.|
|vulkan|26+|8|Optional|Technical preview|It's support for the Vulkan graphics engine.|

### Android manifest
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

### Prerequisites
```kt
ActivityCompat.requestPermissions(this,arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO
), 1)
```

### RTMP Usage
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

## 📓 FAQ
### How can I compile the vulkan module with Android 5 project?
#### AndroidManifest.xml
```xml
<uses-sdk tools:overrideLibrary="com.haishinkit.vulkan" />
```

#### MainActivity
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    PixelTransformFactory.registerPixelTransform(VkPixelTransform::class)
}
```

### RTMP URL Format
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

### Related Project
* HaishinKit.swift - Camera and Microphone streaming library via RTMP, HLS for iOS, macOS and tvOS.
  * https://github.com/shogo4405/HaishinKit.swift

## 💠 Sponsorship
Looking for sponsors. Sponsoring I will enable us to:
- Purchase smartphones or peripheral devices for testing purposes.
- Pay for testing on a specific streaming service or for testing on mobile lines.
- Potentially private use to continue the OSS development

 If you use any of our libraries for work, see if your employers would be interested in sponsorship. I have some special offers.　I would greatly appreciate. Thank you.
 - If you request I will note your name product our README.
 - If you mention on a discussion, an issue or pull request that you are sponsoring us I will prioritise helping you even higher.

スポンサーを募集しています。利用用途としては、
- テスト目的で、スマートフォンの購入や周辺機器の購入を行います。
- 特定のストリーミングサービスへのテストの支払いや、モバイル回線でのテストの支払いに利用します。
- 著書のOSS開発を継続的に行う為に私的に利用する可能性もあります。

このライブラリーを仕事で継続的に利用している場合は、ぜひ。雇用主に、スポンサーに興味がないか確認いただけると幸いです。いくつか特典を用意しています。
- README.mdへの企業ロゴの掲載
- IssueやPull Requestの優先的な対応

[Sponsorship](https://github.com/sponsors/shogo4405)

## 📜 License
BSD-3-Clause
