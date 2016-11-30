package com.haishinkit.rtmp;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.haishinkit.iso.AVCConfigurationRecord;
import com.haishinkit.media.IEncoderListener;
import com.haishinkit.rtmp.messages.RTMPVideoMessage;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RTMPMuxer implements IEncoderListener {
    private final RTMPStream stream;
    private Map<String, Long> timestamps = new ConcurrentHashMap<String, Long>();

    public RTMPMuxer(final RTMPStream stream) {
        this.stream = stream;
    }

    @Override
    public void onFormatChanged(String mime, final MediaFormat mediaFormat) {
        switch (mime) {
            case "video/avc":
                AVCConfigurationRecord config = new AVCConfigurationRecord(mediaFormat);
                RTMPVideoMessage video = new RTMPVideoMessage();
                break;
            default:
                break;
        }
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
