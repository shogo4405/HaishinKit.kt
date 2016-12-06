package com.haishinkit.media;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import org.apache.commons.lang3.builder.ToStringBuilder;

public final class AudioInfo {
    public static final int DEFAULT_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    public static final int DEFAULT_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int DEFAULT_SAMPLING_RATE = 44100;

    private int channel = DEFAULT_CHANNEL;
    private int encoding = DEFAULT_ENCODING;
    private byte[] buffer = null;
    private int samplingRate = DEFAULT_SAMPLING_RATE;
    private int minBufferSize = -1;
    private AudioRecord audioRecord = null;

    public AudioInfo() {
    }

    public final int getChannel() {
        return channel;
    }

    public final AudioInfo setChannel(final int channel) {
        this.channel = channel;
        return this;
    }

    public final int getEncoding() {
        return encoding;
    }

    public final AudioInfo setEncoding(final int encoding) {
        this.encoding = encoding;
        return this;
    }

    public final int getSamplingRate() {
        return samplingRate;
    }

    public final AudioInfo setSamplingRate(final int samplingRate) {
        this.samplingRate = samplingRate;
        return this;
    }

    public final byte[] getBuffer() {
        if (buffer == null) {
            buffer = new byte[getMinBufferSize()];
        }
        return buffer;
    }

    public final int getMinBufferSize() {
        if (minBufferSize == -1) {
            minBufferSize = AudioRecord.getMinBufferSize(getSamplingRate(), getChannel(), getEncoding());
        }
        return minBufferSize;
    }

    public final AudioRecord getAudioRecord() {
        if (audioRecord == null) {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, getSamplingRate(), getChannel(), getEncoding(), getMinBufferSize());
        }
        return audioRecord;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
