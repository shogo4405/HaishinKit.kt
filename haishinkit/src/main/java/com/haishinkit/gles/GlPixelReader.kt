package com.haishinkit.gles

import com.haishinkit.lang.Utilize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.CoroutineContext

internal class GlPixelReader(override var utilizable: Boolean = false) : Utilize, CoroutineScope {
    interface Listener {
        var capturing: Boolean
        fun execute(buffer: ByteBuffer, timestamp: Long)
    }

    var listener: Listener? = null
    val readable: Boolean
        get() = listener?.capturing == true && utilizable
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    private var byteBuffer: ByteBuffer? = null
    private var width = DEFAULT_WIDTH
    private var height = DEFAULT_HEIGHT
    private var job = Job()

    fun setUp(width: Int, height: Int) {
        if (utilizable) { return }
        this.width = width
        this.height = height
        byteBuffer = ByteBuffer.allocateDirect(width * height * 4).apply {
            order(ByteOrder.LITTLE_ENDIAN)
        }
        setUp()
    }

    override fun tearDown() {
        if (!utilizable) { return }
        width = DEFAULT_WIDTH
        height = DEFAULT_HEIGHT
        job.cancel()
        byteBuffer = null
        super.tearDown()
    }

    internal fun read(windowSurface: GlWindowSurface, timestamp: Long) {
        val byteBuffer = byteBuffer ?: return
        windowSurface.readPixels(width, height, byteBuffer)
        launch(coroutineContext) {
            listener?.execute(byteBuffer, timestamp)
        }
    }

    companion object {
        private const val DEFAULT_WIDTH = 0
        private const val DEFAULT_HEIGHT = 0
        private val TAG = GlPixelReader::class.java.simpleName
    }
}
