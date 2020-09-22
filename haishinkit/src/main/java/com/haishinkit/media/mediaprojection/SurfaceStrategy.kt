package com.haishinkit.media.mediaprojection

import android.util.DisplayMetrics
import android.view.Surface
import com.haishinkit.media.Source

internal interface SurfaceStrategy : Source {
    val metrics: DisplayMetrics
    var surface: Surface?
}
