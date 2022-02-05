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
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.media.camera2.CameraResolver
import com.haishinkit.net.NetStream
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
        private set(value) {
            session = null
            field?.close()
            field = value
            stream?.renderer?.pixelTransform?.apply {
                listener = this@Camera2Source
                createInputSurface(resolution.width, resolution.height, IMAGE_FORMAT)
                imageOrientation = this@Camera2Source.imageOrientation
            }
            stream?.videoCodec?.pixelTransform?.imageOrientation = imageOrientation
        }
    var characteristics: CameraCharacteristics? = null
        private set
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
    private var session: CameraCaptureSession? = null
        private set(value) {
            field?.close()
            field = value
            field?.let {
                for (request in requests) {
                    it.setRepeatingRequest(request.build(), null, handler)
                }
            }
        }
    private var requests = mutableListOf<CaptureRequest.Builder>()
    private var surfaces = mutableListOf<Surface>()
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
    fun open(position: Int? = null) {
        if (position == null) {
            this.cameraId = DEFAULT_CAMERA_ID
        } else {
            this.cameraId = resolver.getCameraId(position) ?: DEFAULT_CAMERA_ID
        }
        characteristics = manager.getCameraCharacteristics(cameraId)
        device = null
        manager.openCamera(
            cameraId,
            object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    this@Camera2Source.device = camera
                    this@Camera2Source.setUp()
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
        device = null
        super.tearDown()
    }

    override fun startRunning() {
        Log.d(TAG, "${this::startRunning.name}: $device")
        if (isRunning.get()) {
            return
        }
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

    override fun onPixelTransformSetUp(pixelTransform: PixelTransform) {
        if (stream?.videoCodec?.pixelTransform == pixelTransform) {
            pixelTransform.createInputSurface(resolution.width, resolution.height, IMAGE_FORMAT)
        }
    }

    override fun onPixelTransformInputSurfaceCreated(
        pixelTransform: PixelTransform,
        surface: Surface
    ) {
        if (!surfaces.contains(surface)) {
            surfaces.add(surface)
        }
        createCaptureSession()
    }

    private fun createCaptureSession() {
        val device = device ?: return
        if (surfaces.isEmpty()) {
            return
        }
        for (request in requests) {
            for (surface in surfaces) {
                request.removeTarget(surface)
            }
        }
        requests.clear()
        requests.add(
            device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                surfaces.forEach {
                    addTarget(it)
                }
            }
        )
        device.createCaptureSession(
            surfaces,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    this@Camera2Source.session = session
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

        private const val IMAGE_FORMAT = ImageFormat.YUV_420_888

        private const val DEFAULT_CAMERA_ID = "0"
        private val TAG = Camera2Source::class.java.simpleName
    }
}
