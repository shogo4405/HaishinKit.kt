package com.haishinkit.yuv

import com.haishinkit.codec.BufferInfo
import com.haishinkit.codec.ByteConverter

// https://stackoverflow.com/questions/23107057/rotate-yuv420-nv21-image-in-android
internal class NV21toYUV420SemiPlanarConverter : ByteConverter {
    override fun convert(input: ByteArray, info: BufferInfo): ByteArray {
        var output: ByteArray = ByteArray(input.size)

        val swap = info.rotation == 90 || info.rotation == 270
        val yflip = info.rotation == 90 || info.rotation == 180
        val xflip = info.rotation == 270 || info.rotation == 180

        for (x in 0 until info.width) {
            for (y in 0 until info.height) {
                var xo = x
                var yo = y
                var w = info.width
                var h = info.height
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
