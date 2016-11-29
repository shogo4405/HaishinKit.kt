package com.haishinkit.media;

import android.media.MediaCodec;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import com.haishinkit.lang.IRunnable;

public abstract class EncoderBase implements IRunnable {
    private final String mime;
    private MediaCodec codec = null;
    private AtomicBoolean running = new AtomicBoolean(false);
    private IEncoderDelegate delegate = null;

    public EncoderBase(final String mime) {
        this.mime = mime;
    }

    public IEncoderDelegate getDelegate() {
        return delegate;
    }

    public EncoderBase setDelegate(final IEncoderDelegate delegate) {
        this.delegate = delegate;
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

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
            while (0 <= outputBufferIndex) {
                byte[] outData = new byte[bufferInfo.size];
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                outputBuffer.get(outData);
                outputBuffer.flip();
                if (delegate != null) {
                    delegate.sampleOutput(mime, bufferInfo, outputBuffer);
                }
                codec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract MediaCodec createMediaCodec() throws IOException;
}

