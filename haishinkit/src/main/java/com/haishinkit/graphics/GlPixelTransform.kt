package com.haishinkit.graphics

import android.content.res.AssetManager
import android.graphics.SurfaceTexture
import android.opengl.GLES10
import android.opengl.GLES20
import android.util.Log
import android.view.Surface
import com.haishinkit.BuildConfig
import com.haishinkit.codec.util.DefaultFpsController
import com.haishinkit.codec.util.FpsController
import java.lang.ClassCastException
import android.os.Handler
import android.util.Size
import com.haishinkit.graphics.gles.GlKernel
import com.haishinkit.graphics.gles.GlPixelReader
import com.haishinkit.graphics.gles.GlWindowSurface

internal class GlPixelTransform(
    override var surface: Surface? = null,
    override var inputSurface: Surface? = null,
    override var fpsControllerClass: Class<*>? = null,
    override var assetManager: AssetManager? = null,
    override var listener: PixelTransform.Listener? = null,
) : PixelTransform, SurfaceTexture.OnFrameAvailableListener {
    override var orientation: Int = 0
        get() = kernel.orientation
        set(value) {
            field = value
            kernel.orientation = value
        }
    override var videoGravity: Int = 0
        get() = kernel.videoGravity
        set(value) {
            field = value
            kernel.videoGravity = value
        }
    val reader = GlPixelReader()
    var handler: Handler? = null
    private var kernel: GlKernel = GlKernel()
    private var transform = FloatArray(16)
    private var fpsController: FpsController = DefaultFpsController.instance
    private var inputWindowSurface = GlWindowSurface()
    private var texture: SurfaceTexture? = null
    private var textureId: Int = 0
    private var textureSize: Size = Size(0, 0)

    override fun setUp(surface: Surface?, width: Int, height: Int) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "configuration for ${width}x$height surface=$surface")
        }
        this.surface = surface
        fpsControllerClass?.let {
            if (fpsController is DefaultFpsController) {
                fpsController = try {
                    it.newInstance() as FpsController
                } catch (e: ClassCastException) {
                    fpsController
                }
                Log.d(TAG, fpsController.toString())
            }
        }
        reader.setUp(width, height)
        fpsController.clear()
        inputWindowSurface.tearDown()
        inputWindowSurface.setUp(surface, null)
        inputWindowSurface.makeCurrent()
        kernel.tearDown()
        kernel.resolution = Size(width, height)
        kernel.setUp()
        GLES10.glOrthof(0.0f, width.toFloat(), height.toFloat(), 0.0f, -1.0f, 1.0f)
        listener?.onSetUp(this)
    }

    override fun createInputSurface(width: Int, height: Int, format: Int) {
        val textures = intArrayOf(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        textureSize = Size(width, height)
        val texture = SurfaceTexture(textures[0])
        texture.setOnFrameAvailableListener(this, handler)
        this.texture = texture
        listener?.onCreateInputSurface(this, Surface(texture))
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        if (surface == null) {
            return
        }
        surfaceTexture.updateTexImage()
        var timestamp = surfaceTexture.timestamp
        if (timestamp <= 0L) {
            return
        }
        if (fpsController.advanced(timestamp)) {
            timestamp = fpsController.timestamp(timestamp)
            surfaceTexture.getTransformMatrix(transform)
            kernel.render(textureId, textureSize, transform)
            // inputWindowSurface.setPresentationTime(timestamp)
            if (reader.readable) {
                reader.read(inputWindowSurface, timestamp)
            }
            if (!inputWindowSurface.swapBuffers() && BuildConfig.DEBUG) {
                Log.w(TAG, "can't swap buffers.")
            }
        }
    }

    companion object {
        private val TAG = GlPixelTransform::class.java.simpleName
    }
}