package com.haishinkit.media

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Build
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.screen.Video
import java.util.concurrent.Executors

@RequiresApi(Build.VERSION_CODES.P)
internal class MultiCamera2Output(
    val source: MultiCamera2Source,
    private val cameraId: String,
) : Video.OnSurfaceChangedListener {
    val video: Video by lazy {
        Video().apply {
            isRotatesWithContent = true
            listener = this@MultiCamera2Output
        }
    }

    private var device: CameraDevice? = null
    private val manager = source.context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val executor = Executors.newSingleThreadExecutor()
    private var characteristics: CameraCharacteristics? = null
    private val imageOrientation: ImageOrientation
        get() {
            return when (characteristics?.get(CameraCharacteristics.SENSOR_ORIENTATION)) {
                0 -> ImageOrientation.UP
                90 -> ImageOrientation.LEFT
                180 -> ImageOrientation.DOWN
                270 -> ImageOrientation.RIGHT
                else -> ImageOrientation.UP
            }
        }

    @SuppressLint("MissingPermission")
    fun open() {
        characteristics = manager.getCameraCharacteristics(cameraId)
        manager.openCamera(cameraId, executor, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                this@MultiCamera2Output.device = camera
                video.videoSize = getCameraSize()
                video.imageOrientation = imageOrientation
                source.container.addChild(video)
            }

            override fun onDisconnected(camera: CameraDevice) {
            }

            override fun onError(camera: CameraDevice, error: Int) {
            }
        })
    }

    fun close() {
        device?.close()
    }

    override fun onSurfaceChanged(surface: Surface?) {
        surface?.let {
            createCaptureSession(it)
        }
    }

    private fun getCameraSize(): Size {
        val scm = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val cameraSizes = scm?.getOutputSizes(SurfaceTexture::class.java) ?: return Size(0, 0)
        return cameraSizes[0]
    }

    private fun createCaptureSession(surface: Surface) {
        val device = device ?: return
        val request = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            addTarget(surface)
        }.build()
        val outputList = buildList {
            add(OutputConfiguration(surface))
        }
        device.createCaptureSession(
            SessionConfiguration(SessionConfiguration.SESSION_REGULAR,
                outputList,
                executor,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(captureSession: CameraCaptureSession) {
                        captureSession.setRepeatingRequest(request, null, null)
                    }

                    override fun onConfigureFailed(captureSession: CameraCaptureSession) {
                    }
                })
        )
    }
}
