package com.haishinkit.media

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.MediaCodecInfo
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import com.haishinkit.BuildConfig
import com.haishinkit.codec.MediaCodec
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean

private fun gcd(a: Int, b: Int): Int {
    if (b == 0) return a
    return gcd(b, a % b)
}

/**
 * A video source that captures a camera by the Camera2 API.
 */
class CameraSource(private val manager: CameraManager) : VideoSource {
    var device: CameraDevice? = null
        private set(value) {
            device?.close()
            field = value
        }
    var cameraId: String = DEFAULT_CAMERA_ID
        private set
    var characteristics: CameraCharacteristics? = null
        private set
    var session: CameraCaptureSession? = null
        private set(value) {
            session?.close()
            field = value
            if (value == null) {
                stream?.renderer?.stopRunning()
            } else {
                stream?.renderer?.startRunning()
            }
        }
    val sensorOrientation
        get() = characteristics?.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0

    override var stream: RTMPStream? = null
        set(value) {
            field = value
            stream?.videoCodec?.callback = MediaCodec.Callback()
            stream?.videoCodec?.colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        }
    override val isRunning = AtomicBoolean(false)
    override var resolution = Size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
        set(value) {
            field = value
            stream?.videoSetting?.width = value.width
            stream?.videoSetting?.height = value.height
        }
    private var request: CaptureRequest.Builder? = null
        set(value) {
            request?.let { request ->
                _surface?.let { surface ->
                    request.removeTarget(surface)
                }
            }
            field = value
        }
    private var _surface: Surface? = null
    private var surface: Surface?
        get() {
            if (_surface == null) {
                _surface = stream?.videoCodec?.createInputSurface()
            }
            return _surface
        }
        set(value) {
            _surface = value
        }
    private val backgroundHandler by lazy {
        val thread = HandlerThread(javaClass.name)
        thread.start()
        Handler(thread.looper)
    }

    @SuppressLint("MissingPermission")
    fun open(cameraId: String) {
        this.cameraId = cameraId
        characteristics = manager.getCameraCharacteristics(cameraId)
        manager.openCamera(
            cameraId,
            object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    this@CameraSource.device = camera
                    this@CameraSource.setUp()
                }
                override fun onDisconnected(camera: CameraDevice) {
                    this@CameraSource.device = null
                }
                override fun onError(camera: CameraDevice, error: Int) {
                    Log.w(TAG, error.toString())
                }
            },
            null
        )
    }

    override fun setUp() {
        stream?.renderer?.startRunning()
    }

    override fun tearDown() {
        request = null
        session = null
        device = null
    }

    override fun startRunning() {
        if (isRunning.get()) { return }
        val device = device ?: return
        val surface = surface ?: return
        val rendererSurface = stream?.renderer?.holder?.surface
        request = device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            this.addTarget(surface)
            if (rendererSurface != null) {
                this.addTarget(rendererSurface)
            }
        }
        val surfaceList = mutableListOf<Surface>(surface)
        if (rendererSurface != null) {
            surfaceList.add(rendererSurface)
        }
        device.createCaptureSession(
            surfaceList,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    this@CameraSource.session = session
                    val request = request ?: return
                    session.setRepeatingRequest(request.build(), null, backgroundHandler)
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    this@CameraSource.session = null
                }
            },
            backgroundHandler
        )
        isRunning.set(true)
    }

    override fun stopRunning() {
        if (!isRunning.get()) { return }
        isRunning.set(false)
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    internal fun getPreviewSize(): Size {
        val previewSizes = characteristics
            ?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(SurfaceHolder::class.java) ?: return Size(0, 0)
        var result: Size? = null
        for (previewSize in previewSizes) {
            val gcd = gcd(previewSize.width, previewSize.height)
            val width = previewSize.width / gcd
            val height = previewSize.height / gcd
            if ((height == ASPECT_VERTICAL && width == ASPECT_HORIZONTAL) || (width == ASPECT_VERTICAL && height == ASPECT_HORIZONTAL)) {
                if (result == null) {
                    result = previewSize
                } else {
                    if (result.height < previewSize.height && result.width < previewSize.width) {
                        result = previewSize
                    }
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "$result, list=${ToStringBuilder.reflectionToString(previewSizes)}")
        }
        return result ?: previewSizes[0]
    }

    companion object {
        const val DEFAULT_WIDTH: Int = 640
        const val DEFAULT_HEIGHT: Int = 480

        private const val DEFAULT_CAMERA_ID: String = "0"
        private const val MAX_PREVIEW_WIDTH: Int = 1920
        private const val MAX_PREVIEW_HEIGHT: Int = 1080
        private const val ASPECT_VERTICAL = 16
        private const val ASPECT_HORIZONTAL = 9

        private val TAG = CameraSource::class.java.simpleName
    }
}
