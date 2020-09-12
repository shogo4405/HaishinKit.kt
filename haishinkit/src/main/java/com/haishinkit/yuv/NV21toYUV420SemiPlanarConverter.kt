package com.haishinkit.yuv

import android.util.Log
import com.haishinkit.media.CameraSource
import com.haishinkit.codec.ByteConverter

// https://stackoverflow.com/questions/23107057/rotate-yuv420-nv21-image-in-android
internal class NV21toYUV420SemiPlanarConverter: ByteConverter {
    var width:Int = CameraSource.DEFAULT_WIDTH
    var height:Int = CameraSource.DEFAULT_HEIGHT
    var rotation:Int = 0

    override fun convert(input: ByteArray): ByteArray {
        var output: ByteArray = ByteArray(input.size)

        val swap = rotation == 90 || rotation == 270
        val yflip = rotation == 90 || rotation == 180
        val xflip = rotation == 270 || rotation == 180

        for (x in 0 until width) {
            for (y in 0 until height) {
                var xo = x
                var yo = y
                var w = width
                var h = height
                var xi = xo
                var yi = yo
                if (swap) {
                    xi = w * yo / h
                    yi = h * xo / w
                }
                if (yflip) {
                    yi = h - yi - 1
                }
                if (xflip) {
                    xi = w - xi - 1
                }
                output[w * yo + xo] = input[w * yi + xi]
                val fs = w * h
                val qs = fs shr 2
                xi = xi shr 1
                yi = yi shr 1
                xo = xo shr 1
                yo = yo shr 1
                w = w shr 1
                h = h shr 1
                // adjust for interleave here
                val ui = fs + (w * yi + xi) * 2
                val uo = fs + (w * yo + xo) * 2
                // and here
                val vi = ui + 1
                val vo = uo + 1
                output[uo] = input[vi]
                output[vo] = input[ui]
            }
        }

        return output
    }
}
