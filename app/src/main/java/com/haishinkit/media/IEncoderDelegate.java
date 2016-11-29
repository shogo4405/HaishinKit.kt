package com.haishinkit.media;

import java.nio.ByteBuffer;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;

public interface IEncoderDelegate {
    public void sampleOutput(final String mime, final BufferInfo info, final ByteBuffer buffer);
}
