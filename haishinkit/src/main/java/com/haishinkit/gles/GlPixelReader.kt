package com.haishinkit.gles

import com.haishinkit.lang.Utilize
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class GlPixelReader(override var utilizable: Boolean = false) : Utilize {
    interface Listener {
        fun execute(width: Int, height: Int, buffer: ByteBuffer)
    }

    var listener: Listener? = null
    val readable: Boolean
        get() = listener != null && utilizable
    private var byteBuffer: ByteBuffer? = null
    private var width: Int = DEFAULT_WIDTH
    private var height: Int = DEFAULT_HEIGHT

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
        byteBuffer = null
        super.tearDown()
    }

    internal fun read(windowSurface: GlWindowSurface) {
        val byteBuffer = byteBuffer ?: return
        windowSurface.readPixels(width, height, byteBuffer)
        listener?.execute(width, height, byteBuffer)
    }

    companion object {
        private const val DEFAULT_WIDTH = 0
        private const val DEFAULT_HEIGHT = 0
    }
}
