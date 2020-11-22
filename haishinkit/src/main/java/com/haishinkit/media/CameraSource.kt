package com.haishinkit.media

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import com.haishinkit.BuildConfig
import com.haishinkit.codec.MediaCodec
import com.haishinkit.gles.GlPixelContext
import com.haishinkit.gles.renderer.GlFramePixelRenderer
import com.haishinkit.rtmp.RtmpStream
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * A video source that captures a camera by the Camera2 API.
 */
class CameraSource(
    private val activity: Activity,
    override val fpsControllerClass: Class<*>? = null
) : VideoSource {
    var device: CameraDevice? = null
        private set(value) {
            device?.close()
            field = value
            startRunning()
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
    internal var surface: Surface? = null
    override var stream: RtmpStream? = null
        set(value) {
            field = value
            stream?.videoCodec?.fpsControllerClass = fpsControllerClass
            stream?.videoCodec?.callback = MediaCodec.Callback()
        }
    override val isRunning = AtomicBoolean(false)
    override var resolution = Size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
        set(value) {
            field = value
            stream?.videoSetting?.width = value.width
            stream?.videoSetting?.height = value.height
        }
    private var request: CaptureRequest.Builder? = null
    private var manager: CameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
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
        Log.d(TAG, "${this::startRunning.name}: $device, $surface")
        if (isRunning.get()) { return }
        val device = device ?: return
        val surface = surface ?: return
        request = device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            this.addTarget(surface)
        }
        val surfaceList = mutableListOf<Surface>(surface)
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
        if (BuildConfig.DEBUG) {
            Log.d(TAG, this::startRunning.name)
        }
    }

    override fun stopRunning() {
        if (!isRunning.get()) { return }
        isRunning.set(false)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, this::startRunning.name)
        }
    }

    override fun createGLSurfaceViewRenderer(): VideoSource.GlRenderer {
        return object : VideoSource.GlRenderer {
            override var context: GlPixelContext = GlPixelContext.instance
            override var videoGravity: Int
                get() {
                    return renderer.videoGravity
                }
                set(value) {
                    renderer.videoGravity = value
                }

            private var renderer: GlFramePixelRenderer = GlFramePixelRenderer()

            override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
                renderer.setUp()
                this@CameraSource.startRunning()
                context.textureSize = this@CameraSource.getCameraSize()
            }

            override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
                renderer.resolution = Size(width, height)
            }

            override fun onDrawFrame(gl: GL10) {
                renderer.render(context, FloatArray(16))
            }
        }
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    private fun getCameraSize(): Size {
        val scm = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val cameraSizes = scm?.getOutputSizes(SurfaceTexture::class.java) ?: return Size(0, 0)
        return cameraSizes[0]
    }

    companion object {
        const val DEFAULT_WIDTH: Int = 640
        const val DEFAULT_HEIGHT: Int = 480

        private const val DEFAULT_CAMERA_ID = "0"
        private val TAG = CameraSource::class.java.simpleName
    }
}
