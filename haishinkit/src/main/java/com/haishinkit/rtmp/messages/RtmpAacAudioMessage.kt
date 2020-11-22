package com.haishinkit.rtmp.messages

import com.haishinkit.flv.FlvAudioCodec
import com.haishinkit.flv.FlvSoundRate
import com.haishinkit.flv.FlvSoundSize
import com.haishinkit.flv.FlvSoundType
import com.haishinkit.iso.AudioSpecificConfig
import com.haishinkit.rtmp.RtmpConnection
import java.nio.ByteBuffer

internal class RtmpAacAudioMessage : RtmpAudioMessage() {
    var aacPacketType: Byte = 0
    var config: AudioSpecificConfig? = null
    override var codec = FlvAudioCodec.AAC
    override var soundRate = FlvSoundRate.kHz44
    override var soundSize = FlvSoundSize.SOUND_16BIT
    override var soundType = FlvSoundType.STEREO
    override var length: Int
        get() = 2 + (data?.limit() ?: 0)
        set(value) { super.length = value }

    override fun encode(buffer: ByteBuffer): RtmpMessage {
        buffer.put(AAC)
        buffer.put(aacPacketType)
        data?.let {
            buffer.put(data)
        }
        return this
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
