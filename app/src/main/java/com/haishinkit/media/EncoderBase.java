package com.haishinkit.media;

import android.media.MediaCodec;

import com.haishinkit.iso.AVCConfigurationRecord;

import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class EncoderBase implements IEncoder {
    private final String mime;
    private MediaCodec codec = null;
    private AtomicBoolean running = new AtomicBoolean(false);
    private IEncoderListener listener = null;

    public EncoderBase(final String mime) {
        this.mime = mime;
    }

    public IEncoderListener getListener() {
        return listener;
    }

    public EncoderBase setListener(final IEncoderListener listener) {
        this.listener = listener;
        return this;
    }

    public boolean isRunning() {
        return running.get();
    }

    public final void startRunning() {
        if (running.get()) {
            return;
        }
        try {
            codec = createMediaCodec();
            codec.start();
            running.set(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void stopRunning() {
        if (!running.get()) {
            return;
        }
        codec.stop();
        codec.release();
        codec = null;
        running.set(false);
    }

    public synchronized final void encodeBytes(byte[] data) {
        try {
            ByteBuffer[] inputBuffers = codec.getInputBuffers();
            ByteBuffer[] outputBuffers = codec.getOutputBuffers();

            int inputBufferIndex = codec.dequeueInputBuffer(-1);
            if (0 <= inputBufferIndex) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(data);
                codec.queueInputBuffer(inputBufferIndex, 0, data.length, 0, 0);
            }

            int outputBufferIndex = 0;
            do {
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
                switch (outputBufferIndex) {
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        if (listener != null) {
                            listener.onFormatChanged(mime, codec.getOutputFormat());
                        }
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        break;
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        break;
                    default:
                        if (0 <= outputBufferIndex) {
                            byte[] outData = new byte[bufferInfo.size];
                            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                            outputBuffer.get(outData);
                            outputBuffer.flip();
                            if (listener != null) {
                                listener.onSampleOutput(mime, bufferInfo, outputBuffer);
                            }
                            codec.releaseOutputBuffer(outputBufferIndex, false);
                            outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
                        }
                        break;
                }
            } while (0 <= outputBufferIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract MediaCodec createMediaCodec() throws IOException;
}

