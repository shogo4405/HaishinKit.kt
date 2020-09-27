package com.haishinkit.view

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.haishinkit.lang.Running
import com.haishinkit.media.CameraSource
import com.haishinkit.rtmp.RTMPStream
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A view that previews a camera.
 */
open class CameraView : SurfaceView, Running {
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
            session?.close()
            field = value
            field?.setRepeatingRequest(request!!.build(), null, backgroundHandler)
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
        if (!holder.surface.isValid) { return }
        val source = (stream?.video as CameraSource) ?: return
        val device = source.device ?: return
        request = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            this?.addTarget(holder.surface)
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
    }

    override fun stopRunning() {
        if (!isRunning.get()) { return }
        request = null
        isRunning.set(false)
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }
}
