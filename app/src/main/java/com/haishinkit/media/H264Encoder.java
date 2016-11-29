package com.haishinkit.media;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaCodecInfo;

import java.io.IOException;

public final class H264Encoder extends EncoderBase {
    public static final String MIME = "video/avc";

    public H264Encoder() {
        super(MIME);
    }

    @Override
    protected MediaCodec createMediaCodec() throws IOException {
        MediaCodec codec = MediaCodec.createEncoderByType(MIME);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME, 320, 240);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return codec;
    }
}
