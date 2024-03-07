package com.haishinkit.iso

import com.haishinkit.lang.decodeHex
import com.haishinkit.util.toHexString
import junit.framework.TestCase
import java.nio.ByteBuffer

class HevcDecoderConfigurationRecordTest : TestCase() {
    fun testMediaFormat() {
        val buffer = ByteBuffer.wrap("0000000140010c01ffff0160000003000003000003000003005d2c09000000014201010160000003000003000003000003005da006c201e1cde8b4aec92ee6a0202020571429000000014401c0667c0cc640".decodeHex())
        val record = HevcDecoderConfigurationRecord.create(buffer)
    }

    fun testDecode() {
        val buffer = ByteBuffer.wrap(RECORD.decodeHex())
        val decode = HevcDecoderConfigurationRecord.decode(buffer)
        print(decode)
    }

    fun testToCodecSpecificData() {
        val buffer = ByteBuffer.wrap(RECORD.decodeHex())
        val decode = HevcDecoderConfigurationRecord.decode(buffer)
        val csd0 = decode.toCodecSpecificData(emptyList()).firstOrNull { it.key == "csd-0" } ?: return
        assertEquals(
            "0000000140010c01ffff016000000300b0000003000003005d15c09000000001420101016000000300b0000003000003005da00280802d162057b91655350101010080000000014401c02cbc14c9",
            (csd0.value as ByteBuffer).toHexString()
        )
    }

    companion object {
        const val RECORD = "010160000000b000000000005df000fcfdf8f800000f03200001001840010c01ffff016000000300b0000003000003005d15c0902100010023420101016000000300b0000003000003005da00280802d162057b9165535010101008022000100074401c02cbc14c9"
    }
}
