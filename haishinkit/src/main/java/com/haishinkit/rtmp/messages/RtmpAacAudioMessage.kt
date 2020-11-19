package com.haishinkit.rtmp.messages

import com.haishinkit.flv.AudioCodec
import com.haishinkit.flv.SoundRate
import com.haishinkit.flv.SoundSize
import com.haishinkit.flv.SoundType
import com.haishinkit.iso.AudioSpecificConfig
import com.haishinkit.rtmp.RtmpConnection
import com.haishinkit.rtmp.RtmpSocket
import java.nio.ByteBuffer

internal class RtmpAacAudioMessage : RtmpAudioMessage() {
    var aacPacketType: Byte = 0
    var config: AudioSpecificConfig? = null

    init {
        codec = AudioCodec.AAC
        soundRate = SoundRate.kHz44
        soundSize = SoundSize.SOUND_16BIT
        soundType = SoundType.STEREO
    }

    override fun encode(socket: RtmpSocket): ByteBuffer {
        val length = payload?.limit() ?: 0
        val buffer = ByteBuffer.allocate(2 + length)
        buffer.put(AAC)
        buffer.put(aacPacketType)
        if (0 < length) {
            buffer.put(payload)
        }
        return buffer
    }

    override fun decode(buffer: ByteBuffer): RtmpMessage {
        buffer.position(buffer.position() + length)
        return this
    }

    override fun execute(connection: RtmpConnection): RtmpMessage {
        return this
    }

    companion object {
        private const val AAC = (0x0A shl 4 or (0x03 shl 2) or (0x01 shl 1) or 0x01).toByte()
    }
}
