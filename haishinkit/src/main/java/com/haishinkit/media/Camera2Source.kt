package com.haishinkit.media

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraAccessException
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
import com.haishinkit.gles.GlPixelContext
import com.haishinkit.gles.renderer.GlFramePixelRenderer
import com.haishinkit.media.camera2.CameraResolver
import com.haishinkit.net.NetStream
import com.haishinkit.view.HkGLSurfaceView
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * A video source that captures a camera by the Camera2 API.
 */
class Camera2Source(
    context: Context,
    override val fpsControllerClass: Class<*>? = null,
    override var utilizable: Boolean = false
) : VideoSource {
    var device: CameraDevice? = null
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
    override var stream: NetStream? = null
        set(value) {
            field = value
            stream?.videoCodec?.fpsControllerClass = fpsControllerClass
        }
    override val isRunning = AtomicBoolean(false)
    override var resolution = Size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
        set(value) {
            field = value
            stream?.videoSetting?.width = value.width
            stream?.videoSetting?.height = value.height
        }
    internal var surface: Surface? = null
    private var cameraId: String = DEFAULT_CAMERA_ID
    private var request: CaptureRequest.Builder? = null
    private var manager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val backgroundHandler by lazy {
        val thread = HandlerThread(TAG)
        thread.start()
        Handler(thread.looper)
    }
    private val resolver: CameraResolver by lazy {
        CameraResolver(manager)
    }

    @SuppressLint("MissingPermission")
    fun open(position: Int? = null) {
        if (position == null) {
            this.cameraId = DEFAULT_CAMERA_ID
        } else {
            this.cameraId = resolver.getCameraId(position) ?: DEFAULT_CAMERA_ID
        }
        characteristics = manager.getCameraCharacteristics(cameraId)
        device?.close()
        manager.openCamera(
            cameraId,
            object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    this@Camera2Source.device = camera
                    this@Camera2Source.setUp()
                    surface?.let { it ->
                        this@Camera2Source.createCaptureSession(it, camera)
                    }
                }
                override fun onDisconnected(camera: CameraDevice) {
                    this@Camera2Source.device = null
                }
                override fun onError(camera: CameraDevice, error: Int) {
                    Log.w(TAG, error.toString())
                }
            },
            null
        )
    }

    /**
     * Switches an using camera front or back.
     */
    fun switchCamera() {
        val characteristics = characteristics ?: return
        val facing = resolver.getFacing(characteristics)
        val expect = if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
            CameraCharacteristics.LENS_FACING_BACK
        } else {
            CameraCharacteristics.LENS_FACING_FRONT
        }
        open(expect)
    }

    override fun setUp() {
        if (utilizable) return
        stream?.renderer?.startRunning()
        super.setUp()
    }

    override fun tearDown() {
        if (!utilizable) return
        request = null
        session = null
        device = null
        super.tearDown()
    }

    override fun startRunning() {
        Log.d(TAG, "${this::startRunning.name}: $device, $surface")
        if (isRunning.get()) { return }
        val device = device ?: return
        val surface = surface ?: return
        createCaptureSession(surface, device)
        isRunning.set(true)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, this::startRunning.name)
        }
    }

    override fun stopRunning() {
        if (!isRunning.get()) { return }
        stream?.renderer?.stopRunning()
        session?.let {
            try {
                it.stopRepeating()
            } catch (exception: CameraAccessException) {
                Log.e(TAG, "", exception)
            }
            session = null
        }
        device = null
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
                this@Camera2Source.startRunning()
                context.textureSize = this@Camera2Source.resolver.getCameraSize(this@Camera2Source.characteristics)
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

    private fun createCaptureSession(surface: Surface, device: CameraDevice) {
        request = device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            addTarget(surface)
        }
        val surfaceList = mutableListOf(surface)
        device.createCaptureSession(
            surfaceList,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    this@Camera2Source.session = session
                    val request = request ?: return
                    (this@Camera2Source.stream?.renderer as HkGLSurfaceView).let { view ->
                        val facing = resolver.getFacing(characteristics!!)
                        view.isFront = facing == CameraCharacteristics.LENS_FACING_FRONT
                    }
                    session.setRepeatingRequest(request.build(), null, backgroundHandler)
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    this@Camera2Source.session = null
                }
            },
            backgroundHandler
        )
    }

    companion object {
        const val DEFAULT_WIDTH: Int = 640
        const val DEFAULT_HEIGHT: Int = 480

        private const val DEFAULT_CAMERA_ID = "0"
        private val TAG = Camera2Source::class.java.simpleName
    }
}
