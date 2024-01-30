package com.haishinkit.iso

import java.nio.ByteBuffer

object AvcFormatUtils {
    val START_CODE = byteArrayOf(0, 0, 0, 1)

    private const val ZERO: Byte = 0
    private const val ONE: Byte = 1
    private val TAG = AvcFormatUtils::class.java.simpleName

    fun toNALFile(
        input: ByteBuffer,
        output: ByteBuffer,
    ) {
        var length = 0
        var position = -1
        val offset = output.position()
        val remaining = input.remaining() - 3 + offset
        output.put(input)
        for (i in offset until remaining) {
            if (output.get(i) == ZERO && output.get(i + 1) == ZERO && output.get(i + 2) == ZERO && output.get(
                    i + 3,
                ) == ONE
            ) {
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

    fun toByteStream(
        buffer: ByteBuffer,
        offset: Int,
    ) {
        val position = buffer.position()
        if (0 < offset) {
            buffer.position(position + offset)
        }
        while (buffer.hasRemaining()) {
            val index = buffer.position()
            val length = buffer.int
            buffer.put(index, ZERO)
            buffer.put(index + 1, ZERO)
            buffer.put(index + 2, ZERO)
            buffer.put(index + 3, ONE)
            buffer.position(buffer.position() + length)
        }
        buffer.position(position)
    }
}
