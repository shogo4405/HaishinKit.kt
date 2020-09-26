package com.haishinkit.media

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import com.haishinkit.data.VideoResolution
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A video source that captures a camera by the Camera2 API.
 */
class CameraSource(val manager: CameraManager) : VideoSource {
    var device: CameraDevice? = null
        private set(value) {
            device?.close()
            field = value
            if (value != null) {
                stream?.renderer?.startRunning()
            }
        }
    var cameraId: String = "0"
        @SuppressLint("MissingPermission")
        set(value) {
            field = value
            this.manager.openCamera(value, stateCallback, null)
        }
    override var stream: RTMPStream? = null
    override val isRunning = AtomicBoolean(false)
    override var resolution: VideoResolution = VideoResolution(DEFAULT_WIDTH, DEFAULT_HEIGHT)
    private var request: CaptureRequest.Builder? = null
    private var session: CameraCaptureSession? = null
        set(value) {
            session?.close()
            field = value
        }
    private val stateCallback by lazy {
        object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                this@CameraSource.device = camera
            }
            override fun onDisconnected(camera: CameraDevice) {
                this@CameraSource.device = null
            }
            override fun onError(camera: CameraDevice, error: Int) {
            }
        }
    }
    private val backgroundHandler by lazy {
        var thread = HandlerThread(javaClass.name)
        thread.start()
        Handler(thread.looper)
    }

    override fun setUp() {
        val stream = stream ?: return
        val device = device ?: return
        val surface = stream.videoCodec.createInputSurface() ?: return
        request = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            this.addTarget(surface)
        }
        device.createCaptureSession(
            (Collections.singletonList(surface)),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    this@CameraSource.session = session
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    this@CameraSource.session = null
                }
            },
            null
        )
        isRunning.set(true)
    }

    override fun tearDown() {
    }

    override fun startRunning() {
        if (isRunning.get()) { return }
        val request = request ?: return
        var session = session ?: return
        session.setRepeatingRequest(request.build(), null, backgroundHandler)
        isRunning.set(true)
    }

    override fun stopRunning() {
        if (!isRunning.get()) { return }
        request = null
        session = null
        isRunning.set(false)
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        const val DEFAULT_WIDTH: Int = 640
        const val DEFAULT_HEIGHT: Int = 480
    }
}
