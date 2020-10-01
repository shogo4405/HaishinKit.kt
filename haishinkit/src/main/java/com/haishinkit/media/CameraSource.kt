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
import android.view.Surface
import com.haishinkit.codec.MediaCodec
import com.haishinkit.data.VideoResolution
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean

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
            field = value
            if (value == null) {
                stream?.renderer?.stopRunning()
            } else {
                stream?.renderer?.startRunning()
            }
        }
    override var stream: RTMPStream? = null
        set(value) {
            field = value
            stream?.videoCodec?.callback = MediaCodec.Callback()
            stream?.videoCodec?.colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        }
    override val isRunning = AtomicBoolean(false)
    override var resolution: VideoResolution = VideoResolution(DEFAULT_WIDTH, DEFAULT_HEIGHT)
        set(value) {
            field = value
            stream?.videoSetting?.width = value.width
            stream?.videoSetting?.height = value.height
        }
    private var request: CaptureRequest.Builder? = null
        set(value) {
            surface?.let {
                request?.removeTarget(it)
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
        var thread = HandlerThread(javaClass.name)
        thread.start()
        Handler(thread.looper)
    }

    @SuppressLint("MissingPermission")
    fun open(cameraId: String) {
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
                }
            },
            null
        )
        characteristics = manager.getCameraCharacteristics(cameraId)
        this.cameraId = cameraId
    }

    override fun setUp() {
    }

    override fun tearDown() {
        device = null
        request = null
        session = null
    }

    override fun startRunning() {
        if (isRunning.get()) { return }
        val device = device ?: return
        val surface = surface ?: return
        var rendererSurface = stream?.renderer?.holder?.surface
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

    companion object {
        const val DEFAULT_WIDTH: Int = 640
        const val DEFAULT_HEIGHT: Int = 480

        private const val DEFAULT_CAMERA_ID: String = "0"
    }
}
