package com.haishinkit.rtmp;

import android.media.MediaCodec;
import android.media.MediaFormat;

import com.haishinkit.flv.AACPacketType;
import com.haishinkit.flv.AVCPacketType;
import com.haishinkit.flv.FlameType;
import com.haishinkit.flv.VideoCodec;
import com.haishinkit.iso.AVCConfigurationRecord;
import com.haishinkit.iso.AVCFormatUtils;
import com.haishinkit.iso.AudioSpecificConfig;
import com.haishinkit.media.IEncoderListener;
import com.haishinkit.rtmp.messages.RTMPAACAudioMessage;
import com.haishinkit.rtmp.messages.RTMPAVCVideoMessage;
import com.haishinkit.rtmp.messages.RTMPMessage;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RTMPMuxer implements IEncoderListener {
    private final RTMPStream stream;
    private Map<String, Long> timestamps = new ConcurrentHashMap<String, Long>();
    private AudioSpecificConfig audioConfig = null;
    private AVCConfigurationRecord videoConfig = null;

    public RTMPMuxer(final RTMPStream stream) {
        this.stream = stream;
    }

    @Override
    public final void onFormatChanged(final String mime, final MediaFormat mediaFormat) {
        RTMPMessage message = null;
        switch (mime) {
            case "video/avc":
                videoConfig = new AVCConfigurationRecord(mediaFormat);
                message = new RTMPAVCVideoMessage()
                        .setPacketType(AVCPacketType.SEQ.rawValue())
                        .setFrame(FlameType.KEY.rawValue())
                        .setCodec(VideoCodec.AVC.rawValue())
                        .setPayload(videoConfig.toByteBuffer())
                        .setChunkStreamID(RTMPChunk.VIDEO)
                        .setStreamID(stream.getId());
                break;
            case "audio/mp4a-latm":
                ByteBuffer buffer = mediaFormat.getByteBuffer("csd-0");
                audioConfig = new AudioSpecificConfig(buffer);
                message = new RTMPAACAudioMessage()
                        .setConfig(audioConfig)
                        .setAACPacketType(AACPacketType.SEQ.rawValue())
                        .setPayload(buffer)
                        .setChunkStreamID(RTMPChunk.AUDIO)
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
        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            return;
        }
        int timestamp = 0;
        RTMPMessage message = null;
        if (timestamps.containsKey(mime)) {
            timestamp = new Double((info.presentationTimeUs - timestamps.get(mime).doubleValue())).intValue() / 1000000;
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
            case "audio/mp4a-latm":
                message = new RTMPAACAudioMessage()
                        .setAACPacketType(AACPacketType.RAW.rawValue())
                        .setConfig(audioConfig)
                        .setPayload(buffer)
                        .setChunkStreamID(RTMPChunk.AUDIO)
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
