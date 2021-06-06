package com.haishinkit.media

/**
 * An interface that captures an audio source.
 */
interface AudioSource : Source {
    fun onInputBufferAvailable(codec: android.media.MediaCodec, index: Int)
}
