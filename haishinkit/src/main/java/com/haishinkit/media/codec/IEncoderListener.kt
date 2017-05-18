package com.haishinkit.media.codec

import java.nio.ByteBuffer

import android.media.MediaCodec.BufferInfo
import android.media.MediaFormat

interface IEncoderListener {
    fun onFormatChanged(mime: String, mediaFormat: MediaFormat)
    fun onSampleOutput(mime: String, info: BufferInfo, buffer: ByteBuffer)
}
