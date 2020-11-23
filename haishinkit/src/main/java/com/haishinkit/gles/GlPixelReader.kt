package com.haishinkit.gles

import com.haishinkit.lang.Utilize
import java.nio.ByteBuffer

internal class GlPixelReader(override var utilizable: Boolean = false) : Utilize {
    interface Listener {
        fun onRead(width: Int, height: Int, byteBuffer: ByteBuffer)
    }

    var listner: Listener? = null
    val readable: Boolean
        get() = listner != null && utilizable
    private var byteBuffer: ByteBuffer? = null
    private var width: Int = 0
    private var height: Int = 0

    fun setUp(width: Int, height: Int) {
        if (utilizable) { return }
        this.width = width
        this.height = height
        byteBuffer = ByteBuffer.allocateDirect(width * height * 32)
        setUp()
    }

    override fun tearDown() {
        if (!utilizable) { return }
        width = 0
        height = 0
        byteBuffer = null
        super.tearDown()
    }

    fun read(windowSurface: GlWindowSurface) {
        val byteBuffer = byteBuffer ?: return
        windowSurface.readPixels(width, height, byteBuffer)
        listner?.onRead(width, height, byteBuffer)
    }
}
