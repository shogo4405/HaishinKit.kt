package com.haishinkit.metric

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.haishinkit.codec.RecordSetting
import com.haishinkit.lang.Utilize
import com.haishinkit.net.NetStream
import com.haishinkit.net.NetStream.Listener
import com.haishinkit.util.FileUtil
import com.haishinkit.util.ZipUtil
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class FrameCapture(
    override var utilizable: Boolean = false
) : Listener, Utilize {
    private var index = DEFAULT_INDEX
    private var uuid = UUID.randomUUID().toString()
    private var metadata: FileOutputStream? = null

    override fun onSetUp(stream: NetStream) {
        if (utilizable) return
        uuid = UUID.randomUUID().toString()
        try {
            File(stream.recordSetting.directory, uuid).mkdir()
            metadata = FileOutputStream(File(stream.recordSetting.directory, "$uuid/metadata.csv"))
        } catch (e: IOException) {
            Log.d(TAG, "", e)
        }
        super.setUp()
    }

    override fun onTearDown(stream: NetStream) {
        if (!utilizable) return
        val workspace = File(stream.recordSetting.directory, uuid)
        ZipUtil.zipFile(workspace.toPath())
        FileUtil.delete(workspace)
        index = DEFAULT_INDEX
        super.tearDown()
    }

    override fun onCaptureOutput(
        stream: NetStream,
        type: Byte,
        buffer: ByteBuffer,
        timestamp: Long
    ) {
        try {
            val file = createFile(stream.recordSetting)
            val builder =
                StringBuilder().append(file.name).append(",").append(timestamp).append("\n")
            metadata?.write(builder.toString().toByteArray(StandardCharsets.UTF_8))
            val outStream = FileOutputStream(file)
            outStream.write(buffer.array())
            outStream.close()
            index++
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "", e)
        } catch (e: IOException) {
            Log.d(TAG, "", e)
        }
    }

    private fun createFile(record: RecordSetting): File {
        return File(record.directory, "$uuid/v-%010d.rgba".format(index))
    }

    companion object {
        const val DEFAULT_INDEX = 1L
        private val TAG = FrameCapture::class.java.simpleName
    }
}
