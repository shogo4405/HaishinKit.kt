package com.haishinkit.util;

import java.nio.ByteBuffer;

public final class ByteBufferUtils {
    private ByteBufferUtils() {
    }

    public static String toHexString(final  ByteBuffer buffer) {
        StringBuilder builder = new StringBuilder();
        ByteBuffer slice = buffer.slice();
        for (int i = 0; i < slice.limit(); ++i) {
            builder.append(String.format("0x%02x,", slice.get() & 0xff));
        }
        return builder.toString();
    }
}
