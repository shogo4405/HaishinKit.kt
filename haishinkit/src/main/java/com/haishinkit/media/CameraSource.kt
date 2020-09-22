package com.haishinkit.media

import android.graphics.ImageFormat
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import com.haishinkit.codec.BufferInfo
import com.haishinkit.codec.BufferType
import com.haishinkit.data.VideoResolution
import com.haishinkit.media.util.CameraUtils
import com.haishinkit.media.util.MediaCodecUtils
import com.haishinkit.rtmp.RTMPStream
import java.util.concurrent.atomic.AtomicBoolean

class CameraSource : VideoSource, SurfaceHolder.Callback, android.hardware.Camera.PreviewCallback {
    var camera: android.hardware.Camera?

    override var stream: RTMPStream? = null
        set(value) {
            field = value
            stream?.videoCodec?.width = actualResolution.width
            stream?.videoCodec?.height = actualResolution.height
        }
    override val isRunning = AtomicBoolean(false)

    override var resolution: VideoResolution = VideoResolution(DEFAULT_WIDTH, DEFAULT_HEIGHT)
    var actualResolution: VideoResolution = VideoResolution(DEFAULT_WIDTH, DEFAULT_HEIGHT)
        private set
    internal var displayOrientation: Int = 0

    constructor(camera: android.hardware.Camera) {
        this.camera = camera
    }

    override fun setUp() {
        if (camera == null) {
            return
        }
        actualResolution = CameraUtils.getActualSize(resolution, camera!!.parameters.supportedPreviewSizes)
        val parameters = camera!!.parameters
        parameters.setPreviewSize(actualResolution.width, actualResolution.height)
        parameters.setPreviewFpsRange(30000, 30000)
        for (format in camera!!.parameters.supportedPreviewFormats) {
            Log.v(javaClass.name, MediaCodecUtils.imageFormatToString(format))
        }
        parameters.previewFormat = ImageFormat.NV21
        camera?.parameters = parameters
    }

    override fun tearDown() {
    }

    override fun startRunning() {
        isRunning.set(true)
    }

    override fun stopRunning() {
        isRunning.set(false)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        try {
            camera?.setPreviewCallback(this)
            camera?.setPreviewDisplay(holder)
        } catch (e: Exception) {
            Log.w(javaClass.name, "", e)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        camera?.stopPreview()
        if (-1 < displayOrientation) {
            camera?.setDisplayOrientation(displayOrientation)
        }
        camera?.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun onPreviewFrame(bytes: ByteArray?, camera: Camera?) {
        stream?.appendBytes(
            bytes,
            BufferInfo(
                type = BufferType.VIDEO,
                presentationTimeUs = System.nanoTime() / 1000000L,
                width = actualResolution.width,
                height = actualResolution.height,
                rotation = displayOrientation
            )
        )
    }

    companion object {
        const val DEFAULT_WIDTH: Int = 640
        const val DEFAULT_HEIGHT: Int = 480
    }
}
