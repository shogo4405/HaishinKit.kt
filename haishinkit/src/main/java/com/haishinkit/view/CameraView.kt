package com.haishinkit.view

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.Log
import android.view.*
import com.haishinkit.BuildConfig
import com.haishinkit.lang.Running
import com.haishinkit.media.CameraSource
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean

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

    companion object {
        private val TAG = CameraView::class.java.simpleName
    }
}
