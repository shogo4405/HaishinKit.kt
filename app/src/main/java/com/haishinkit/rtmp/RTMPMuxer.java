package com.haishinkit.rtmp;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.haishinkit.flv.AVCPacketType;
import com.haishinkit.flv.FlameType;
import com.haishinkit.flv.VideoCodec;
import com.haishinkit.iso.AVCConfigurationRecord;
import com.haishinkit.iso.AVCFormatUtils;
import com.haishinkit.media.IEncoderListener;
import com.haishinkit.rtmp.messages.RTMPAVCVideoMessage;
import com.haishinkit.rtmp.messages.RTMPMessage;
import com.haishinkit.util.ByteBufferUtils;
import com.haishinkit.util.Log;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RTMPMuxer implements IEncoderListener {
    private final RTMPStream stream;
    private Map<String, Long> timestamps = new ConcurrentHashMap<String, Long>();

    public RTMPMuxer(final RTMPStream stream) {
        this.stream = stream;
    }

    @Override
    public final void onFormatChanged(final String mime, final MediaFormat mediaFormat) {
        RTMPMessage message = null;
        switch (mime) {
            case "video/avc":
                AVCConfigurationRecord config = new AVCConfigurationRecord(mediaFormat);
                message = new RTMPAVCVideoMessage()
                        .setPacketType(AVCPacketType.SEQ.rawValue())
                        .setFrame(FlameType.KEY.rawValue())
                        .setCodec(VideoCodec.AVC.rawValue())
                        .setPayload(config.toByteBuffer())
                        .setChunkStreamID(RTMPChunk.VIDEO)
                        .setStreamID(stream.getId());
                break;
            default:
                break;
        }
        if (message != null) {
            stream.connection.getSocket().doOutput(RTMPChunk.ZERO, message);
        }
    }

    @Override
    public final void onSampleOutput(final String mime, final MediaCodec.BufferInfo info, final ByteBuffer buffer) {
        int timestamp = 0;
        RTMPMessage message = null;
        if (timestamps.containsKey(mime)) {
            timestamp = new Double(info.presentationTimeUs - timestamps.get(mime).doubleValue()).intValue();
        }
        switch (mime) {
            case "video/avc":
                boolean keyframe = (info.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
                message = new RTMPAVCVideoMessage()
                        .setPacketType(AVCPacketType.NAL.rawValue())
                        .setFrame(keyframe ? FlameType.KEY.rawValue() : FlameType.INTER.rawValue())
                        .setCodec(VideoCodec.AVC.rawValue())
                        .setPayload(AVCFormatUtils.toNALFileFormat(buffer))
                        .setChunkStreamID(RTMPChunk.VIDEO)
                        .setTimestamp(timestamp)
                        .setStreamID(stream.getId());
                break;
            default:
                break;
        }
        if (message != null) {
            stream.connection.getSocket().doOutput(RTMPChunk.ONE, message);
        }
        timestamps.put(mime, info.presentationTimeUs);
    }

    public void clear() {
        timestamps.clear();
    }
}
