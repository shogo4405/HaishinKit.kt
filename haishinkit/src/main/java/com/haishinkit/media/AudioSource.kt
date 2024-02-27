package com.haishinkit.media

import com.haishinkit.codec.AudioCodec

/**
 * An interface that captures an audio source.
 */
interface AudioSource : Source {
    fun registerAudioCodec(codec: AudioCodec)
    fun unregisterAudioCodec(codec: AudioCodec)
}
