package com.haishinkit.net;

import com.haishinkit.util.Log;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class Socket {
    protected ByteBuffer inputBuffer = null;
    private java.net.Socket socket = null;
    private Thread output = null;
    private Thread network = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private BlockingQueue<ByteBuffer> outputQueue = null;

    public Socket() {
        inputBuffer = ByteBuffer.allocate(0);
        outputQueue = new ArrayBlockingQueue<ByteBuffer>(128);
    }

    public final void connect(final String dstName, final int dstPort) {
        outputQueue.clear();
        network = new Thread() {
            public void run() {
                doConnection(dstName, dstPort);
            }
        };
        network.start();
    }

    public final void close() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(getClass().getName(), e.toString());
        }
    }

    public void doOutput(ByteBuffer buffer) {
        try {
            outputQueue.put(buffer);
        } catch (InterruptedException e) {
            Log.v(getClass().getName(), e.toString());
        }
    }

    protected abstract void onConnect();
    protected abstract void listen(ByteBuffer buffer);

    private void doInput() {
        try {
            int available = inputStream.available();
            if (available == 0) {
                return;
            }
            ByteBuffer buffer = ByteBuffer.allocate(inputBuffer.capacity() + available);
            buffer.put(inputBuffer);
            inputStream.read(buffer.array(), inputBuffer.capacity(), available);
            buffer.position(0);
            listen(buffer);
            inputBuffer = buffer.slice();
        } catch (IOException e) {
            Log.e(getClass().getName(), e.toString());
        }
    }

    private void doOutput() {
        while (socket != null && socket.isConnected()) {
            for (ByteBuffer buffer : outputQueue) {
                try {
                    buffer.flip();
                    outputStream.write(buffer.array());
                    outputStream.flush();
                    outputQueue.remove(buffer);
                } catch (IOException e) {
                    Log.e(getClass().getName(), e.toString());
                }
            }
        }
    }

    private void doConnection(final String dstName, final int dstPort) {
        Log.v(getClass().getName(), dstName + ":" + dstPort);
        try {
            socket = new java.net.Socket(dstName, dstPort);
            if (socket.isConnected()) {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                output = new Thread() {
                    @Override
                    public void run() {
                        doOutput();
                    }
                };
                output.start();
                onConnect();
            }
            while (socket != null && socket.isConnected()) {
                doInput();
            }
        } catch (Exception e) {
            Log.v(getClass().getName(), e.toString());
        }
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}


