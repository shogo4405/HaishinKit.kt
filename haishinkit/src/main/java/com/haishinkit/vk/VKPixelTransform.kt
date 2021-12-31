package com.haishinkit.vk

import android.content.res.AssetManager
import android.view.Choreographer
import android.view.Surface
import com.haishinkit.lang.PixelTransform

class VKPixelTransform : PixelTransform, Choreographer.FrameCallback {
    companion object {
        init {
            System.loadLibrary("haishinkit")
        }

        external fun isSupported(): Boolean
    }

    var surface: Surface?
        external get
        external set

    var inputSurface: Surface?
        external get
        external set

    @Suppress("unused")
    private var memory: Long = 0
    private var isRunning: Boolean = false
    private var choreographer: Choreographer? = null

    external fun setAssetManager(assetManager: AssetManager)

    fun startRunning() {
        if (isRunning) {
            return
        }
        choreographer = Choreographer.getInstance()
        choreographer?.postFrameCallback(this)
        isRunning = true
    }

    fun stopRunning() {
        if (!isRunning) {
            return
        }
        choreographer?.removeFrameCallback(this)
        isRunning = false
    }

    override fun doFrame(frameTimeNanos: Long) {
        updateTexture()
        choreographer?.postFrameCallback(this)
    }

    protected fun finalize() {
        dispose()
    }

    private external fun updateTexture()
    private external fun dispose()
}
