package com.haishinkit.media;

import java.io.IOException;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaCodecInfo;
import android.provider.MediaStore;

public final class H264Encoder extends EncoderBase {
    public static final String MIME = "video/avc";
    public static final int DEFAULT_BIT_RATE = 125000;
    public static final int DEFAULT_FRAME_RATE = 15;
    public static final int DEFAULT_I_FRAME_INTERVAL = 2;
    public static final int DEFAULT_WIDTH = 320;
    public static final int DEFAULT_HEIGHT = 240;
    public static final int DEFAULT_PROFILE = MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline;
    public static final int DEFAULT_LEVEL = MediaCodecInfo.CodecProfileLevel.AVCLevel31;

    private int bitRate = DEFAULT_BIT_RATE;
    private int frameRate = DEFAULT_FRAME_RATE;
    private int IFrameInterval = DEFAULT_I_FRAME_INTERVAL;
    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    private int profile = DEFAULT_PROFILE;
    private int level = DEFAULT_LEVEL;

    public H264Encoder() {
        super(MIME);
    }

    public int getBitRate() {
        return bitRate;
    }

    public H264Encoder setBitRate(int bitRate) {
        this.bitRate = bitRate;
        return this;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public H264Encoder setFrameRate(int frameRate) {
        this.frameRate = frameRate;
        return this;
    }

    public int getIFrameInterval() {
        return IFrameInterval;
    }

    public H264Encoder setIFrameInterval(int IFrameInterval) {
        this.IFrameInterval = IFrameInterval;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public H264Encoder setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public H264Encoder setHeight(int height) {
        this.height = height;
        return this;
    }

    public int getProfile() {
        return profile;
    }

    public H264Encoder setProfile(final int profile) {
        this.profile = profile;
        return this;
    }

    public int getLevel() {
        return level;
    }

    public H264Encoder setLevel(final int level) {
        this.level = level;
        return this;
    }

    @Override
    protected MediaCodec createMediaCodec() throws IOException {
        MediaCodec codec = MediaCodec.createEncoderByType(MIME);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME, getWidth(), getHeight());
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, getBitRate());
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, getFrameRate());
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, getIFrameInterval());
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, getProfile());
        mediaFormat.setInteger(MediaFormat.KEY_AAC_ENCODED_TARGET_LEVEL, getLevel());
        codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return codec;
    }
}
