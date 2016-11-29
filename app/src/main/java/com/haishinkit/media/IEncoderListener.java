package com.haishinkit.media;

import java.nio.ByteBuffer;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

public interface IEncoderListener {
    public void onFormatChanged(final String mime, final MediaFormat mediaFormat);
    public void onSampleOutput(final String mime, final BufferInfo info, final ByteBuffer buffer);
}
