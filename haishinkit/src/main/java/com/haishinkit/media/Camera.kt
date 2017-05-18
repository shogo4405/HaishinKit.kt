package com.haishinkit.media

import android.graphics.ImageFormat
import android.hardware.Camera
import android.view.SurfaceHolder
import com.haishinkit.yuv.NV21toYUV420SemiPlanarConverter
import com.haishinkit.media.util.CameraUtils
import com.haishinkit.media.util.MediaCodecUtils
import com.haishinkit.rtmp.RTMPStream
import com.haishinkit.util.Log
import com.haishinkit.util.Size

class Camera: SurfaceHolder.Callback, android.hardware.Camera.PreviewCallback, IDevice {
    var camera: android.hardware.Camera?
    internal var displayOrientaion:Int = 0
    internal var stream: RTMPStream? = null

    var size: Size = Size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
    var actualSize: Size = Size(DEFAULT_WIDTH, DEFAULT_HEIGHT)
        private set

    constructor(camera: android.hardware.Camera) {
        this.camera = camera
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        try {
            camera?.setPreviewCallback(this)
            camera?.setPreviewDisplay(holder)
        } catch (e:Exception) {
            Log.w(javaClass.name, "", e)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        camera?.stopPreview()
        if (-1 < displayOrientaion) {
            camera?.setDisplayOrientation(displayOrientaion)
            val byteConverter = stream?.getEncoderByName("video/avc")?.byteConverter
            if (byteConverter is NV21toYUV420SemiPlanarConverter) {
                byteConverter.rotation = displayOrientaion
            }
        }
        camera?.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun onPreviewFrame(bytes: ByteArray?, camera: Camera?) {
        stream?.appendBytes(bytes, System.nanoTime() / 1000000L, RTMPStream.BufferType.VIDEO)
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

    companion object {
        var DEFAULT_WIDTH:Int = 640
        var DEFAULT_HEIGHT:Int = 480
    }
}

