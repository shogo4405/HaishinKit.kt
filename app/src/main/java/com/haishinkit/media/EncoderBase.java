package com.haishinkit.media;

import android.media.MediaCodec;

import com.haishinkit.util.Log;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.lang.Runnable;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class EncoderBase implements IEncoder, Runnable {
    private final String mime;
    private Thread dequeue = null;
    private MediaCodec codec = null;
    private AtomicBoolean running = new AtomicBoolean(false);
    private IEncoderListener listener = null;

    public EncoderBase(final String mime) {
        this.mime = mime;
    }

    public final IEncoderListener getListener() {
        return listener;
    }

    public final EncoderBase setListener(final IEncoderListener listener) {
        this.listener = listener;
        return this;
    }

    public final boolean isRunning() {
        return running.get();
    }

    public final void startRunning() {
        synchronized (this) {
            if (running.get()) {
                return;
            }
            try {
                dequeue = new Thread(this);
                codec = createMediaCodec();
                codec.start();
                running.set(true);
                dequeue.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public final void stopRunning() {
        synchronized (this) {
            if (!running.get()) {
                return;
            }
            codec.stop();
            codec.release();
            codec = null;
            running.set(false);
            dequeue = null;
        }
    }

    public synchronized final void encodeBytes(byte[] data, long presentationTimeUs) {
        if (!running.get()) {
            return;
        }
        try {
            final ByteBuffer[] inputBuffers = codec.getInputBuffers();
            final int inputBufferIndex = codec.dequeueInputBuffer(-1);
            if (0 <= inputBufferIndex) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(data);
                codec.queueInputBuffer(inputBufferIndex, 0, data.length, presentationTimeUs, 0);
            }
        } catch (Exception e) {
            Log.w(getClass().getName(), e.toString());
        }
    }

    public void run() {
        ByteBuffer[] outputBuffers = codec.getOutputBuffers();
        while (running.get()) {
            final MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            final int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, -1);
            switch (outputBufferIndex) {
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d(getClass().getName(), "INFO_OUTPUT_FORMAT_CHANGED");
                    if (listener != null) {
                        listener.onFormatChanged(mime, codec.getOutputFormat());
                    }
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.d(getClass().getName(), "INFO_TRY_AGAIN_LATER");
                    break;
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d(getClass().getName(), "OUTPUT_BUFFERS_CHANGED");
                    outputBuffers = codec.getOutputBuffers();
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
                    }
                    break;
            }
        }
    }

    protected abstract MediaCodec createMediaCodec() throws IOException;
}

