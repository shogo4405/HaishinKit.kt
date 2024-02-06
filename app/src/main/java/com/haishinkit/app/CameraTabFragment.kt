package com.haishinkit.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.airbnb.lottie.ImageAssetDelegate
import com.airbnb.lottie.LottieImageAsset
import com.haishinkit.event.Event
import com.haishinkit.event.EventUtils
import com.haishinkit.event.IEventListener
import com.haishinkit.graphics.effect.DefaultVideoEffect
import com.haishinkit.graphics.effect.MonochromeVideoEffect
import com.haishinkit.media.AudioRecordSource
import com.haishinkit.media.Camera2Source
import com.haishinkit.media.Stream
import com.haishinkit.media.StreamDrawable
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpStream
import com.haishinkit.screen.Image
import com.haishinkit.lottie.LottieScreen
import com.haishinkit.screen.Screen
import com.haishinkit.screen.ScreenObject
import com.haishinkit.screen.Text
import com.haishinkit.util.Rectangle
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class CameraTabFragment : Fragment(), IEventListener, ImageAssetDelegate {
    private class Callback(private val fragment: CameraTabFragment) : Screen.Callback() {
        private val dateFormat = SimpleDateFormat("HH:mm:ss")
        override fun onEnterFrame() {
            try {
                fragment.text.textValue = dateFormat.format(Date())
                // Log.e("TAG", fragment.lottie.isAnimating.toString())
            } catch (e: RuntimeException) {
                Log.e(TAG, "", e)
            }
        }
    }

    private lateinit var connection: RtmpConnection
    private lateinit var stream: RtmpStream
    private lateinit var cameraView: StreamDrawable
    private lateinit var cameraSource: Camera2Source
    private val text: Text by lazy { Text() }
    private val lottie: LottieScreen by lazy { LottieScreen(requireContext()) }
    private val callback: Screen.Callback by lazy { Callback(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            val permissionCheck = ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.CAMERA), 1)
            }
            if (ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
            }
        }
        connection = RtmpConnection()
        stream = RtmpStream(requireContext(), connection)
        stream.attachAudio(AudioRecordSource(requireContext()))

        cameraSource = Camera2Source(requireContext())
        stream.attachVideo(cameraSource)
        stream.screen.frame = Rectangle(
            Point(0, 0),
            Size(Stream.DEFAULT_SCREEN_HEIGHT, Stream.DEFAULT_SCREEN_WIDTH)
        )

        text.textSize = 60f
        text.textValue = ""
        text.layoutMargins.set(0, 0, 16, 16)
        text.horizontalAlignment = ScreenObject.HORIZONTAL_ALIGNMENT_RIGHT
        text.verticalAlignment = ScreenObject.VERTICAL_ALIGNMENT_BOTTOM

        val image = Image()
        image.bitmap = BitmapFactory.decodeResource(resources, R.drawable.game_jikkyou)
        image.verticalAlignment = ScreenObject.VERTICAL_ALIGNMENT_BOTTOM
        image.frame.set(0, 0, 180, 180)
        stream.screen.addChild(image)

        stream.screen.addChild(text)
        stream.screen.registerCallback(callback)

        lottie.setAnimation(R.raw.a1707142754988)
        lottie.frame.set(0, 0, 200, 200)
        lottie.setImageAssetDelegate(this)
        lottie.playAnimation()
        stream.screen.addChild(lottie)

        connection.addEventListener(Event.RTMP_STATUS, this)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
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

        val save = v.findViewById<Button>(R.id.save_button)
        save.setOnClickListener {
            stream.screen.readPixels {
                val bitmap = it ?: return@readPixels

                val file = File(requireContext().externalCacheDir, "share_temp.jpeg")
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.flush()
                }

                val fileUri: Uri? = try {
                    FileProvider.getUriForFile(
                        requireContext(), requireContext().packageName + ".fileprovider", file
                    )
                } catch (e: IllegalArgumentException) {
                    null
                }

                fileUri ?: run {
                    return@readPixels
                }

                val builder =
                    this@CameraTabFragment.activity?.let { it1 -> ShareCompat.IntentBuilder.from(it1) }
                        ?.apply {
                            addStream(fileUri)
                            setType(requireContext().contentResolver.getType(fileUri))
                        }?.createChooserIntent()?.apply {
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            resolveActivity(requireContext().packageManager)?.also {
                                startActivity(this)
                            }
                        }
            }
        }

        val filter = v.findViewById<Button>(R.id.filter_button)
        filter.setOnClickListener {
            if (filter.text == "Normal") {
                stream.videoEffect = MonochromeVideoEffect()
                filter.text = "Mono"
            } else {
                stream.videoEffect = DefaultVideoEffect.shared
                filter.text = "Normal"
            }
        }

        val switchButton = v.findViewById<Button>(R.id.switch_button)
        switchButton.setOnClickListener { cameraSource.switchCamera() }
        cameraView = if (Preference.useSurfaceView) {
            v.findViewById(R.id.surface_view)
        } else {
            v.findViewById(R.id.texture_view)
        }
        cameraView.attachStream(stream)
        return v
    }

    override fun onResume() {
        super.onResume()
        cameraSource.open(CameraCharacteristics.LENS_FACING_BACK)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                stream.screen.frame = Rectangle(
                    Point(0, 0),
                    Size(Stream.DEFAULT_SCREEN_HEIGHT, Stream.DEFAULT_SCREEN_WIDTH)
                )
            }

            Configuration.ORIENTATION_LANDSCAPE -> {
                stream.screen.frame = Rectangle(
                    Point(0, 0),
                    Size(Stream.DEFAULT_SCREEN_WIDTH, Stream.DEFAULT_SCREEN_HEIGHT)
                )
            }

            Configuration.ORIENTATION_SQUARE -> {
            }

            Configuration.ORIENTATION_UNDEFINED -> {
            }
        }
    }

    override fun onPause() {
        cameraSource.close()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.dispose()
    }

    override fun handleEvent(event: Event) {
        Log.i("$TAG#handleEvent", event.data.toString())
        val data = EventUtils.toMap(event)
        val code = data["code"].toString()
        if (code == RtmpConnection.Code.CONNECT_SUCCESS.rawValue) {
            stream.publish(Preference.shared.streamName)
        }
    }

    override fun fetchBitmap(asset: LottieImageAsset?): Bitmap? {
        Log.e("TAG", asset.toString())
        return null
    }

    companion object {

        private val TAG = CameraTabFragment::class.java.simpleName

        fun newInstance(): CameraTabFragment {
            return CameraTabFragment()
        }
    }
}
