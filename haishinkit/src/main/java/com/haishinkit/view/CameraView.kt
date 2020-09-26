package com.haishinkit.view

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.haishinkit.lang.Running
import com.haishinkit.media.CameraSource
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A view that previews a camera.
 */
open class CameraView : SurfaceView, Running {
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    private var session: CameraCaptureSession? = null
    private var request: CaptureRequest.Builder? = null
    private var stream: RTMPStream? = null
        set(value) {
            stream?.renderer = null
            field = value
            field?.renderer = this
        }
    private val stateCallback by lazy {
        object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                this@CameraView.session = session
                this@CameraView.startPreview()
            }
            override fun onConfigureFailed(session: CameraCaptureSession) {
            }
        }
    }
    private val backgroundHandler by lazy {
        var thread = HandlerThread(javaClass.name)
        thread.start()
        Handler(thread.looper)
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                this@CameraView.startRunning()
            }
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }
            override fun surfaceDestroyed(holder: SurfaceHolder?) {
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
        try {
            val device = (stream?.video as CameraSource)?.device ?: return
            request = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                this.addTarget(holder.surface)
            }
            device.createCaptureSession((Collections.singletonList(holder.surface)), stateCallback, null)
            isRunning.set(true)
        } catch (e: IllegalArgumentException) {
            Log.d(javaClass.name, "", e)
        }
    }

    override fun stopRunning() {
        if (!isRunning.get()) { return }
        session?.stopRepeating()
        session = null
        isRunning.set(false)
    }

    private fun startPreview() {
        val session = session ?: return
        val request = request ?: return
        session.setRepeatingRequest(request.build(), null, backgroundHandler)
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
