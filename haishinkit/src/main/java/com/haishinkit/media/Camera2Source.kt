package com.haishinkit.media

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
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
import com.haishinkit.screen.Video
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A video source that captures a camera by the Camera2 API.
 */
@Suppress("MemberVisibilityCanBePrivate")
class Camera2Source(context: Context) : VideoSource, CameraDevice.StateCallback(),
    Video.OnSurfaceChangedListener {

    /**
     * Specifies the listener indicates the [Camera2Source.Listener] are currently being evaluated.
     */
    var listener: Listener? = null
    var device: CameraDevice? = null
        private set(value) {
            session = null
            field?.close()
            field = value
        }
    var characteristics: CameraCharacteristics? = null
        private set
    override var stream: Stream? = null
    override val isRunning = AtomicBoolean(false)
    override val screen: Video by lazy {
        Video().apply {
            isRotatesWithContent = true
        }
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
    private var session: CameraCaptureSession? = null
        private set(value) {
            field?.close()
            field = value
            field?.let {
                for (request in requests) {
                    try {
                        it.setRepeatingRequest(request.build(), null, handler)
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "", e)
                    }
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
        stream?.screen?.removeChild(screen)
        if (position == null) {
            this.cameraId = DEFAULT_CAMERA_ID
        } else {
            this.cameraId = getCameraId(position) ?: DEFAULT_CAMERA_ID
        }
        characteristics = manager.getCameraCharacteristics(cameraId)
        device = null
        manager.openCamera(cameraId, this, handler)
    }

    fun close() {
        device = null
    }

    /**
     * Switches an using camera front or back.
     */
    fun switchCamera() {
        val facing = getFacing()
        val expect = if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
            CameraCharacteristics.LENS_FACING_BACK
        } else {
            CameraCharacteristics.LENS_FACING_FRONT
        }
        open(expect)
    }

    override fun startRunning() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "${this::startRunning.name}: $device")
        }
        if (isRunning.get()) return
        screen.listener = this
        isRunning.set(true)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, this::startRunning.name)
        }
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        session?.let {
            try {
                it.stopRepeating()
            } catch (exception: CameraAccessException) {
                Log.e(TAG, "", exception)
            }
            session = null
        }
        device = null
        screen.listener = null
        isRunning.set(false)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, this::startRunning.name)
        }
    }

    override fun onOpened(camera: CameraDevice) {
        device = camera
        surfaces.clear()
        screen.videoSize = getCameraSize()
        screen.imageOrientation = imageOrientation
        screen.surface?.let {
            createCaptureSession(it)
        }
        stream?.screen?.addChild(screen)
        stream?.screen?.bringChildToFront(screen)
    }

    override fun onDisconnected(camera: CameraDevice) {
        device = null
    }

    override fun onError(camera: CameraDevice, error: Int) {
        listener?.onError(camera, error)
        device = null
    }

    override fun onSurfaceChanged(surface: Surface?) {
        surface?.let {
            createCaptureSession(it)
        }
    }

    private fun createCaptureSession(surface: Surface) {
        if (Thread.currentThread() != handler?.looper?.thread) {
            handler?.post {
                createCaptureSession(surface)
            }
            return
        }
        if (!surfaces.contains(surface)) {
            surfaces.add(surface)
        }
        val device = device ?: return
        for (request in requests) {
            for (surface in surfaces) {
                request.removeTarget(surface)
            }
        }
        requests.clear()
        requests.add(device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
            listener?.onCreateCaptureRequest(this)
            surfaces.forEach {
                addTarget(it)
            }
        })
        device.createCaptureSession(
            surfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    this@Camera2Source.session = session
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    this@Camera2Source.session = null
                }
            }, handler
        )
    }

    private fun getCameraId(facing: Int): String? {
        for (id in manager.cameraIdList) {
            val chars = manager.getCameraCharacteristics(id)
            if (chars.get(CameraCharacteristics.LENS_FACING) == facing) {
                return id
            }
        }
        return null
    }

    private fun getFacing(): Int? {
        return characteristics?.get(CameraCharacteristics.LENS_FACING)
    }

    private fun getCameraSize(): Size {
        val scm = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val cameraSizes = scm?.getOutputSizes(SurfaceTexture::class.java) ?: return Size(0, 0)
        return cameraSizes[0]
    }

    /**
     * The Listener interface is the primary method for handling events.
     */
    interface Listener {
        /**
         * Tells the receiver to error.
         */
        fun onError(camera: CameraDevice, error: Int)

        /**
         * Tells the receiver to create a capture request.
         */
        fun onCreateCaptureRequest(builder: CaptureRequest.Builder)
    }

    companion object {
        private const val IMAGE_FORMAT = 0x00000022 // AIMAGE_FORMAT_PRIVATE
        private const val DEFAULT_CAMERA_ID = "0"
        private val TAG = Camera2Source::class.java.simpleName
    }
}
