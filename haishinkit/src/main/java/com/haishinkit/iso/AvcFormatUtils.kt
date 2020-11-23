package com.haishinkit.iso

import java.nio.ByteBuffer

object AvcFormatUtils {
    fun put(input: ByteBuffer, output: ByteBuffer) {
        var length = 0
        var position = -1
        val offset = output.position()
        val remaining = input.remaining() - 3 + offset
        output.put(input)
        for (i in offset until remaining) {
            if (output.get(i).toInt() == 0x00 && output.get(i + 1).toInt() == 0x00 && output.get(i + 2).toInt() == 0x00 && output.get(i + 3).toInt() == 0x01) {
                if (0 <= position) {
                    output.putInt(position, length - 3)
                }
                position = i
                length = 0
            } else {
                ++length
            }
        }
        output.putInt(position, length)
    }
}
