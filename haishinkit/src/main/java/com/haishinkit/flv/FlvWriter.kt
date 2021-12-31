package com.haishinkit.flv

import android.util.Log
import com.haishinkit.codec.RecordSetting
import com.haishinkit.rtmp.message.RtmpAudioMessage
import com.haishinkit.rtmp.message.RtmpDataMessage
import com.haishinkit.rtmp.message.RtmpMessage
import com.haishinkit.rtmp.message.RtmpVideoMessage
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class FlvWriter : Closeable {
    private var fileOutputStream: FileOutputStream? = null
    private var previousTagSize = 0
    private var audioTimestamp = 0
    private var videoTimestamp = 0

    fun open(setting: RecordSetting, fileName: String) {
        val directory = setting.directory ?: return
        previousTagSize = 0
        audioTimestamp = 0
        videoTimestamp = 0
        try {
            fileOutputStream = FileOutputStream(File(directory, "$fileName.flv"))
            fileOutputStream?.write(SIGNATURE)
            fileOutputStream?.write(byteArrayOf(0x01, 0b00000101.toByte()))
            fileOutputStream?.write(HEADER_SIZE shr 24)
            fileOutputStream?.write(HEADER_SIZE shr 16)
            fileOutputStream?.write(HEADER_SIZE shr 8)
            fileOutputStream?.write(HEADER_SIZE)
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "", e)
        } catch (e: IOException) {
            Log.d(TAG, "", e)
        }
    }

    override fun close() {
        try {
            fileOutputStream?.close()
        } catch (e: IOException) {
            Log.d(TAG, "", e)
        }
    }

    @Synchronized
    internal fun write(message: RtmpMessage): FlvWriter {
        val fileOutputStream = fileOutputStream ?: return this
        if (!(message is RtmpAudioMessage || message is RtmpVideoMessage || message is RtmpDataMessage)) {
            return this
        }
        try {
            val length = message.length
            val timestamp = when (true) {
                (message is RtmpAudioMessage) -> {
                    audioTimestamp
                }
                (message is RtmpVideoMessage) -> {
                    videoTimestamp
                }
                else -> {
                    0
                }
            }
            fileOutputStream.write(previousTagSize shr 24)
            fileOutputStream.write(previousTagSize shr 16)
            fileOutputStream.write(previousTagSize shr 8)
            fileOutputStream.write(previousTagSize)
            fileOutputStream.write(message.type.toInt())
            fileOutputStream.write(length shr 16)
            fileOutputStream.write(length shr 8)
            fileOutputStream.write(length.toInt())
            fileOutputStream.write(timestamp shr 16)
            fileOutputStream.write(timestamp shr 8)
            fileOutputStream.write(timestamp)
            fileOutputStream.write(0x00)
            fileOutputStream.write(STREAM_ID)
            fileOutputStream.write(message.payload.array(), 0, length)
            previousTagSize += 11 + length
            if (message is RtmpAudioMessage) {
                audioTimestamp += message.timestamp
            } else if (message is RtmpVideoMessage) {
                videoTimestamp += message.timestamp
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "", e)
        } catch (e: IOException) {
            Log.d(TAG, "", e)
        }
        return this
    }

    companion object {
        private val SIGNATURE: ByteArray = byteArrayOf(0x46, 0x4C, 0x56)
        private val STREAM_ID: ByteArray = byteArrayOf(0x00, 0x00, 0x00)
        private const val HEADER_SIZE: Int = 9
        private val TAG = FlvWriter::class.java.simpleName
    }
}
