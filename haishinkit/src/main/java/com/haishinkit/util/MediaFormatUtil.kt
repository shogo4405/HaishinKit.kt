package com.haishinkit.util

import android.media.MediaFormat

object MediaFormatUtil {
    private const val CROP_LEFT = "crop-left"
    private const val CROP_RIGHT = "crop-right"
    private const val CROP_TOP = "crop-top"
    private const val CROP_BOTTOM = "crop-bottom"

    fun getWidth(format: MediaFormat): Int {
        var width = format.getInteger(MediaFormat.KEY_WIDTH)
        if (format.containsKey(CROP_LEFT) && format.containsKey(CROP_RIGHT)) {
            width = format.getInteger(CROP_RIGHT) + 1 - format.getInteger(CROP_LEFT)
        }
        return width
    }

    fun getHeight(format: MediaFormat): Int {
        var height = format.getInteger(MediaFormat.KEY_HEIGHT)
        if (format.containsKey(CROP_TOP) && format.containsKey(CROP_BOTTOM)) {
            height = format.getInteger(CROP_BOTTOM) + 1 - format.getInteger(CROP_TOP)
        }
        return height
    }
}
