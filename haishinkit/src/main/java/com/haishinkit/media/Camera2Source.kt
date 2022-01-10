package com.haishinkit.media

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
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
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.media.camera2.CameraResolver
import com.haishinkit.net.NetStream
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A video source that captures a camera by the Camera2 API.
 */
class Camera2Source(
    private val context: Context,
    override val fpsControllerClass: Class<*>? = null,
    override var utilizable: Boolean = false
) : VideoSource, PixelTransform.Listener {
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
    private var cameraId: String = DEFAULT_CAMERA_ID
    private var manager: CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var handler: Handler? = null
        get() {
            if (field == null) {
                val thread = HandlerThread(TAG)
                thread.start()
                field = Handler(thread.looper)
            }
            return field
        }
        set(value) {
            field?.looper?.quitSafely()
            field = value
        }
    private val resolver: CameraResolver by lazy {
        CameraResolver(manager)
    }
    private var requests = mutableListOf<CaptureRequest.Builder>()
    private var surfaceList = mutableListOf<Surface>()

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
                    stream?.renderer?.pixelTransform?.listener = this@Camera2Source
                    stream?.renderer?.pixelTransform?.createInputSurface(
                        resolution.width,
                        resolution.height,
                        0x1
                    )
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
        stream?.videoCodec?.setAssetManager(context.assets)
        stream?.videoCodec?.setListener(this)
        stream?.renderer?.startRunning()
        super.setUp()
    }

    override fun tearDown() {
        if (!utilizable) return
        session = null
        device = null
        super.tearDown()
    }

    override fun startRunning() {
        Log.d(TAG, "${this::startRunning.name}: $device")
        if (isRunning.get()) {
            return
        }
        val device = device ?: return
        isRunning.set(true)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, this::startRunning.name)
        }
    }

    override fun stopRunning() {
        if (!isRunning.get()) {
            return
        }
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

    override fun onSetUp(pixelTransform: PixelTransform) {
        if (stream?.videoCodec?.pixelTransform == pixelTransform) {
            pixelTransform.createInputSurface(resolution.width, resolution.height, 0x1)
        }
    }

    override fun onCreateInputSurface(pixelTransform: PixelTransform, surface: Surface) {
        device?.let {
            createCaptureSession(surface, it)
        }
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    private fun createCaptureSession(surface: Surface, device: CameraDevice) {
        surfaceList.add(surface)
        requests.add(device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            surfaceList.forEach {
                addTarget(it)
            }
        })
        device.createCaptureSession(
            surfaceList,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    this@Camera2Source.session = session
                    for (request in requests) {
                        session.setRepeatingRequest(request.build(), null, handler)
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    this@Camera2Source.session = null
                }
            },
            handler
        )
    }

    companion object {
        const val DEFAULT_WIDTH: Int = 640
        const val DEFAULT_HEIGHT: Int = 480

        private const val DEFAULT_CAMERA_ID = "0"
        private val TAG = Camera2Source::class.java.simpleName
    }
}
