package com.haishinkit.iso

import java.nio.ByteBuffer

object AvcFormatUtils {
    fun toNALFileFormat(buffer: ByteBuffer): ByteBuffer {
        val result = ByteBuffer.allocate(buffer.remaining())
        result.put(buffer)
        result.flip()
        var length = 0
        var position = -1
        val remaining = result.remaining() - 3
        for (i in 0..remaining - 1) {
            if (result.get(i).toInt() == 0x00 && result.get(i + 1).toInt() == 0x00 && result.get(i + 2).toInt() == 0x00 && result.get(i + 3).toInt() == 0x01) {
                if (0 <= position) {
                    result.putInt(position, length - 3)
                }
                position = i
                length = 0
            } else {
                ++length
            }
        }
        result.putInt(position, length)
        return result
    }
}
