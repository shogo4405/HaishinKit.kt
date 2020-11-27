package com.haishinkit.flv

/**
 * The type of flv supports audio sound rates.
 */
object FlvSoundRate {
    /**
     * The sound rate of  5,500.0kHz
     */
    const val kHz5_5: Byte = 0x00

    /**
     * Ths sound rate of 11,000.0kHz.
     */
    const val kHz11: Byte = 0x01

    /**
     * The sound rate of 22,050.0kHz.
     */
    const val kHz22: Byte = 0x02

    /**
     * Ths sound rate of 44,100.0kHz.
     */
    const val kHz44: Byte = 0x03
}
