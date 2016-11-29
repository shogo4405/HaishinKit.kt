package com.haishinkit.media;

import com.haishinkit.lang.IRunnable;

public interface IEncoder extends IRunnable {
    public IEncoderListener getDelegate();
    public IEncoder setDelegate(final IEncoderListener delegate);
    public void encodeBytes(byte[] data);
}
