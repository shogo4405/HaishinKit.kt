package com.haishinkit.codec

import android.media.MediaCodec.BufferInfo
import android.media.MediaFormat
import java.nio.ByteBuffer

interface IEncoderListener {
    fun onFormatChanged(mime: String, mediaFormat: MediaFormat)
    fun onSampleOutput(mime: String, info: BufferInfo, buffer: ByteBuffer)
}
