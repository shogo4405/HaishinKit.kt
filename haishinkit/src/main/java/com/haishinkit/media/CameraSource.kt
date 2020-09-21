package com.haishinkit.media

import android.graphics.ImageFormat
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import com.haishinkit.codec.BufferInfo
import com.haishinkit.codec.BufferType
import com.haishinkit.media.util.CameraUtils
import com.haishinkit.media.util.MediaCodecUtils
import com.haishinkit.rtmp.RTMPStream
import com.haishinkit.util.Size

class CameraSource : VideoSource, SurfaceHolder.Callback, android.hardware.Camera.PreviewCallback {
    var camera: android.hardware.Camera?

    override var stream: RTMPStream? = null
        set(value) {
            field = value
            stream?.videoCodec?.width = actualSize.width
            stream?.videoCodec?.height = actualSize.height
        }
    override var isRunning: Boolean = false

    var size: Size = Size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
    var actualSize: Size = Size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
        private set
    internal var displayOrientation: Int = 0

    constructor(camera: android.hardware.Camera) {
        this.camera = camera
    }

    override fun setUp() {
        if (camera == null) {
            return
        }
        actualSize = CameraUtils.getActualSize(size, camera!!.parameters.supportedPreviewSizes)
        val parameters = camera!!.parameters
        parameters.setPreviewSize(actualSize.width, actualSize.height)
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
        isRunning = true
    }

    override fun stopRunning() {
        isRunning = false
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
                width = actualSize.width,
                height = actualSize.height,
                rotation = displayOrientation
            )
        )
    }

    companion object {
        const val DEFAULT_WIDTH: Int = 640
        const val DEFAULT_HEIGHT: Int = 480
    }
}
