package com.haishinkit.media.util

import android.hardware.Camera
import android.util.Log
import com.haishinkit.data.VideoResolution

object CameraUtils {
    fun getActualSize(videoResolution: VideoResolution, supportedPreviewSizes: List<Camera.Size>): VideoResolution {
        val rate = videoResolution.width.toDouble() / videoResolution.height.toDouble()
        for (s in supportedPreviewSizes) {
            Log.v(javaClass.name + "#supportedPreviewSizes", s.width.toString() + ":" + s.height.toString())
        }
        for (s in supportedPreviewSizes) {
            val r = videoResolution.width.toDouble() / videoResolution.height.toDouble()
            if (r == rate && s.width <= videoResolution.width && s.height <= videoResolution.width) {
                return VideoResolution(s.width, s.height)
            }
        }
        return VideoResolution(supportedPreviewSizes.last().width, supportedPreviewSizes.last().height)
    }
}
