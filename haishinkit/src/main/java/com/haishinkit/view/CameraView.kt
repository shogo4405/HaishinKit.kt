package com.haishinkit.view

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.Surface
import android.view.WindowManager
import com.haishinkit.rtmp.RTMPStream
import com.haishinkit.util.Log

class CameraView: SurfaceView, SurfaceHolder.Callback {
    private var stream: RTMPStream? = null

    constructor(context: Context, attributes: AttributeSet): super(context, attributes) {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        stream?.camera?.surfaceCreated(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        stream?.camera?.displayOrientaion = getCameraDisplayOrientaion()
        stream?.camera?.surfaceChanged(holder, format, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        stream?.camera?.surfaceDestroyed(holder)
    }

    fun attachStream(stream:RTMPStream) {
        this.stream = stream
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val previewSize = stream?.camera?.camera?.parameters?.previewSize
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

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(widthSize, widthMode),
                MeasureSpec.makeMeasureSpec(heightSize, heightMode)
        )
    }

    private fun getCameraDisplayOrientaion():Int {
        if (stream?.camera == null) {
            return -1
        }

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
}
