package com.haishinkit.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.*
import com.haishinkit.BuildConfig
import com.haishinkit.lang.Running
import com.haishinkit.media.CameraSource
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

/**
 * A view that previews a camera.
 */
open class CameraView(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes), Running {
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    private var request: CaptureRequest.Builder? = null
    private var stream: RTMPStream? = null
        set(value) {
            stream?.renderer = null
            field = value
            field?.renderer = this
        }
    private var session: CameraCaptureSession? = null
        set(value) {
            Log.d(TAG, value.toString())
            session?.close()
            field = value
            request?.let {
                field?.setRepeatingRequest(it.build(), null, backgroundHandler)
            }
        }
    private val backgroundHandler by lazy {
        val thread = HandlerThread(TAG)
        thread.start()
        Handler(thread.looper)
    }
    private val requireDimensionSwapped: Boolean
        get() {
            val source = stream?.video as CameraSource
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayRotation = windowManager.defaultDisplay?.rotation ?: 0
            val sensorOrientation = source.sensorOrientation
            when (displayRotation) {
                Surface.ROTATION_0, Surface.ROTATION_180 -> {
                    if (sensorOrientation == 90 || sensorOrientation == 270) {
                        return true
                    }
                }
                Surface.ROTATION_90, Surface.ROTATION_270 -> {
                    if (sensorOrientation == 0 || sensorOrientation == 180) {
                        return true
                    }
                }
            }
            return false
        }

    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                this@CameraView.startRunning()
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                this@CameraView.stopRunning()
            }
        })
    }

    open fun attachStream(stream: RTMPStream?) {
        this.stream = stream
        if (stream != null) {
            startRunning()
        } else {
            stopRunning()
        }
    }

    override fun startRunning() {
        if (isRunning.get()) { return }
        if (!holder.surface.isValid) { return }
        val source = (stream?.video as CameraSource) ?: return
        val device = source.device ?: return
        request = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            this.addTarget(holder.surface)
        }
        val previewSize = source.getPreviewSize()
        if (requireDimensionSwapped) {
            setAspectRatio(previewSize.height, previewSize.width)
        } else {
            setAspectRatio(previewSize.width, previewSize.height)
        }
        device.createCaptureSession(
            listOf(holder.surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    this@CameraView.session = session
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    this@CameraView.session = null
                }
            },
            null
        )
        isRunning.set(true)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "startRunning()")
        }
    }

    override fun stopRunning() {
        if (!isRunning.get()) { return }
        request = null
        session = null
        isRunning.set(false)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "stopRunning()")
        }
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    private var aspectRatio = 0f

    /**
     * Sets the aspect ratio for this view. The size of the view will be
     * measured based on the ratio calculated from the parameters.
     *
     * @param width  Camera resolution horizontal size
     * @param height Camera resolution vertical size
     */
    fun setAspectRatio(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Size cannot be negative" }
        aspectRatio = width.toFloat() / height.toFloat()
        holder.setFixedSize(width, height)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (aspectRatio == 0f) {
            setMeasuredDimension(width, height)
        } else {
            // Performs center-crop transformation of the camera frames
            val newWidth: Int
            val newHeight: Int
            val actualRatio = if (width > height) aspectRatio else 1f / aspectRatio
            if (width < height * actualRatio) {
                newHeight = height
                newWidth = (height * actualRatio).roundToInt()
            } else {
                newWidth = width
                newHeight = (width / actualRatio).roundToInt()
            }
            Log.d(TAG, "Measured dimensions set: $newWidth x $newHeight")
            setMeasuredDimension(newWidth, newHeight)
        }
    }

    companion object {
        private val TAG = CameraView::class.java.simpleName
    }
}
