package com.haishinkit.media.codec

import com.haishinkit.lang.IRunnable

interface IEncoder : IRunnable {
    var listener: IEncoderListener?
    var byteConverter: ByteConverter?
    fun encodeBytes(data: ByteArray, presentationTimeUs: Long)
}
