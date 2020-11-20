package com.haishinkit.gles

import android.content.Context
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.util.Size
import android.view.Surface
import android.view.WindowManager
import com.haishinkit.lang.Utilize

class GlPixelContext(private val context: Context? = null, private val swapped: Boolean) : Utilize {
    override var utilizable = false
    var textureId: Int = 0
        private set
    var textureSize = Size(0, 0)
    val orientation: Int
        get() {
            context ?: return ROTATION_0
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            return when (windowManager.defaultDisplay?.rotation ?: -1) {
                Surface.ROTATION_0 -> if (isPortrait) ROTATION_270 else ROTATION_0
                Surface.ROTATION_90 -> if (isPortrait) ROTATION_0 else ROTATION_90
                Surface.ROTATION_180 -> if (isPortrait) ROTATION_90 else ROTATION_180
                Surface.ROTATION_270 -> if (isPortrait) ROTATION_180 else ROTATION_270
                else -> 0
            }
        }
    private var isPortrait: Boolean = false

    init {
        if (context != null && swapped) {
            isPortrait = if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                (orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_180)
            } else {
                (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270)
            }
        }
    }

    override fun setUp() {
        if (utilizable) return

        val textures = intArrayOf(0)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        super.setUp()
    }

    override fun tearDown() {
        if (!utilizable) return

        textureId = 0

        super.tearDown()
    }

    fun createSurfaceTexture(width: Int, height: Int): SurfaceTexture {
        return SurfaceTexture(textureId).apply {
            setDefaultBufferSize(width, height)
        }
    }

    companion object {
        var instance = GlPixelContext(null, false)

        const val ROTATION_0 = 0
        const val ROTATION_90 = 1
        const val ROTATION_180 = 2
        const val ROTATION_270 = 3

        private val TAG = GlPixelContext::class.java.simpleName
    }
}
