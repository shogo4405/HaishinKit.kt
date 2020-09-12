package com.haishinkit.media.util

import android.util.Log
import android.hardware.Camera
import com.haishinkit.util.Size

object CameraUtils {
    fun getActualSize(size: Size, supportedPreviewSizes:List<Camera.Size>): Size {
        val rate = size.width.toDouble() / size.height.toDouble()
        for (s in supportedPreviewSizes) {
            Log.v(javaClass.name + "#supportedPreviewSizes", s.width.toString() + ":" + s.height.toString())
        }
        for (s in supportedPreviewSizes) {
            val r = size.width.toDouble() / size.height.toDouble()
            if (r == rate && s.width <= size.width && s.height <= size.width) {
                return Size(s.width, s.height)
            }
        }
        return Size(supportedPreviewSizes.last().width, supportedPreviewSizes.last().height)
    }
}
