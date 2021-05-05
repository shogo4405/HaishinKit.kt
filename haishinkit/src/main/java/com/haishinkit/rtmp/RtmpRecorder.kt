package com.haishinkit.rtmp

import com.haishinkit.codec.RecordSetting
import com.haishinkit.flv.FlvWriter
import com.haishinkit.lang.Running
import com.haishinkit.rtmp.messages.RtmpMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

internal class RtmpRecorder(override val isRunning: AtomicBoolean = AtomicBoolean(false)) : Running, CoroutineScope, Closeable {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    private var job = Job()
    private var writer = FlvWriter()

    fun open(setting: RecordSetting, fileName: String) {
        launch(coroutineContext) {
            writer.open(setting, fileName)
        }
    }

    fun write(message: RtmpMessage) {
        launch(coroutineContext) {
            writer.write(message)
            message.release()
        }
    }

    override fun close() {
        launch(coroutineContext) {
            writer.close()
        }
    }

    override fun startRunning() {
        if (isRunning.get()) return
        isRunning.set(true)
    }

    override fun stopRunning() {
        if (!isRunning.get()) return
        isRunning.set(false)
    }

    companion object {
        private val TAG = RtmpRecorder::class.java.simpleName
    }
}
