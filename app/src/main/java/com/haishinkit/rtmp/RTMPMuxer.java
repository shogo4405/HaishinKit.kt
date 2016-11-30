package com.haishinkit.rtmp;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.haishinkit.flv.AVCPacketType;
import com.haishinkit.flv.FlameType;
import com.haishinkit.flv.VideoCodec;
import com.haishinkit.iso.AVCConfigurationRecord;
import com.haishinkit.media.IEncoderListener;
import com.haishinkit.rtmp.messages.RTMPAVCVideoMessage;
import com.haishinkit.rtmp.messages.RTMPMessage;

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
    public void onFormatChanged(final String mime, final MediaFormat mediaFormat) {
        RTMPMessage message = null;
        switch (mime) {
            case "video/avc":
                AVCConfigurationRecord config = new AVCConfigurationRecord(mediaFormat);
                message = new RTMPAVCVideoMessage()
                        .setFrame(FlameType.COMMAND.rawValue())
                        .setCodec(VideoCodec.AVC.rawValue())
                        .setFrame(AVCPacketType.SEQ.rawValue())
                        .setPayload(config.toByteBuffer())
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
    public void onSampleOutput(final String mime, final MediaCodec.BufferInfo info, final ByteBuffer buffer) {
        int timestamp = 0;
        RTMPChunk chunk = RTMPChunk.ZERO;
        RTMPMessage message = null;
        if (timestamps.containsKey(mime)) {
            chunk = RTMPChunk.ONE;
            timestamp = new Double(info.presentationTimeUs - timestamps.get(mime).doubleValue()).intValue();
        }
        switch (mime) {
            case "video/avc":
                boolean keyframe = (info.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0;
                message = new RTMPAVCVideoMessage()
                        .setPacketType(AVCPacketType.NAL.rawValue())
                        .setFrame(keyframe ? FlameType.KEY.rawValue() : FlameType.INTER.rawValue())
                        .setCodec(VideoCodec.AVC.rawValue())
                        .setPayload(buffer)
                        .setChunkStreamID(RTMPChunk.VIDEO)
                        .setTimestamp(timestamp)
                        .setStreamID(stream.getId());
                break;
            default:
                break;
        }
        if (message != null) {
            stream.connection.getSocket().doOutput(chunk, message);
        }
        timestamps.put(mime, info.presentationTimeUs);
    }

    public void clear() {
        timestamps.clear();
    }
}
