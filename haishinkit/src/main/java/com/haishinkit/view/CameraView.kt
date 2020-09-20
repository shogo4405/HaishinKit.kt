package com.haishinkit.view

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import com.haishinkit.media.CameraSource
import com.haishinkit.rtmp.RTMPStream

class CameraView : SurfaceView, SurfaceHolder.Callback {
    private var camera: CameraSource? = null
    private var displayOrientation: Int = 0
        get() {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(0, info)

            val winManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            var degrees = 0

            when (winManager.defaultDisplay.rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }

            var result: Int
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                return (360 - result) % 360
            }

            return (info.orientation - degrees + 360) % 360
        }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        holder.addCallback(this)
    }

    fun attachStream(stream: RTMPStream?) {
        this.camera = stream?.video as CameraSource
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        camera?.surfaceCreated(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        camera?.displayOrientation = displayOrientation
        camera?.surfaceChanged(holder, format, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        camera?.surfaceDestroyed(holder)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val previewSize = camera?.camera?.parameters?.previewSize
        if (previewSize == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val aspectRadio = previewSize.height.toDouble() / previewSize.width.toDouble()
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = (widthSize * aspectRadio).toInt()

        setMeasuredDimension(widthSize, heightSize)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, widthMode), MeasureSpec.makeMeasureSpec(heightSize, heightMode))
    }
}
