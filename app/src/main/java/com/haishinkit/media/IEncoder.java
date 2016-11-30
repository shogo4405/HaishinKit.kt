package com.haishinkit.media;

import com.haishinkit.lang.IRunnable;

public interface IEncoder extends IRunnable {
    public IEncoderListener getListener();
    public IEncoder setListener(final IEncoderListener listener);
    public void encodeBytes(byte[] data);
}
