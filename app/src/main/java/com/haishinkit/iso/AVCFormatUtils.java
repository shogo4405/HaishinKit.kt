package com.haishinkit.iso;

import com.haishinkit.util.Log;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class AVCFormatUtils {
    private AVCFormatUtils() {
    }

    public final static ByteBuffer toNALFileFormat(final ByteBuffer buffer) {
        ByteBuffer result = ByteBuffer.allocate(buffer.remaining());
        result.put(buffer);
        result.flip();
        int length = 0;
        int position = -1;
        int remaining = result.remaining() - 3;
        for (int i = 0; i < remaining; ++i) {
            if (result.get(i) == 0x00 && result.get(i + 1) == 0x00 && result.get(i + 2) == 0x00 && result.get(i + 3) == 0x01) {
                if (0 <= position) {
                    result.putInt(position, length - 3);
                }
                position = i;
                length = 0;
            } else {
                ++length;
            }
        }
        result.putInt(position, length);
        return result;
    }
}
