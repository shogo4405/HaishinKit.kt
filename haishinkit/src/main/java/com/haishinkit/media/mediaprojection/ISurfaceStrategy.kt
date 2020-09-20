package com.haishinkit.media.mediaprojection

import android.util.DisplayMetrics
import android.view.Surface
import com.haishinkit.media.DeviceSource

internal interface ISurfaceStrategy : DeviceSource {
    val metrics: DisplayMetrics
    var surface: Surface?
}
