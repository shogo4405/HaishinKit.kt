package com.haishinkit.media.camera2

import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Size

class CameraResolver(private val manager: CameraManager) {
    fun getCameraId(facing: Int): String? {
        for (id in manager.cameraIdList) {
            val chars = manager.getCameraCharacteristics(id)
            if (chars.get(CameraCharacteristics.LENS_FACING) == facing) {
                return id
            }
        }
        return null
    }

    fun getFacing(characteristics: CameraCharacteristics): Int? {
        return characteristics.get(CameraCharacteristics.LENS_FACING)
    }

    fun getCameraSize(characteristics: CameraCharacteristics?): Size {
        val scm = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val cameraSizes = scm?.getOutputSizes(SurfaceTexture::class.java) ?: return Size(0, 0)
        return cameraSizes[0]
    }
}
