package com.haishinkit.rtmp;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.haishinkit.media.IEncoderListener;

import java.nio.ByteBuffer;

public class RTMPMuxer implements IEncoderListener {
    private final RTMPStream stream;

    public RTMPMuxer(final RTMPStream stream) {
        this.stream = stream;
    }

    @Override
    public void onFormatChanged(final MediaFormat mediaFormat) {

    }

    @Override
    public void onSampleOutput(String mime, MediaCodec.BufferInfo info, ByteBuffer buffer) {
        switch (mime) {
            case "video/avc":
                break;
            default:
                break;
        }
    }
}
