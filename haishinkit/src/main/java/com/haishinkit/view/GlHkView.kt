package com.haishinkit.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import com.haishinkit.BuildConfig
import com.haishinkit.gles.GlPixelContext
import com.haishinkit.media.CameraSource
import com.haishinkit.media.VideoSource
import com.haishinkit.rtmp.RtmpStream
import com.haishinkit.util.VideoGravity
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * A view that displays a video content of a NetStream object which uses OpenGL api.
 */
class GlHkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : GLSurfaceView(context, attrs), NetStreamView {
    internal interface StrategyRenderer : GLSurfaceView.Renderer {
        var strategy: VideoSource.GlRenderer
        var videoGravity: Int
    }

    override var videoGravity: Int = VideoGravity.RESIZE_ASPECT_FILL
        set(value) {
            renderer.videoGravity = value
            field = value
        }
    override val isRunning: AtomicBoolean = AtomicBoolean(false)
    override var stream: RtmpStream? = null

    private val renderer: StrategyRenderer by lazy {
        val renderer = object : StrategyRenderer {
            override var strategy: VideoSource.GlRenderer = VideoSource.NullRenderer.instance
                set(value) {
                    val videoGravity = field.videoGravity
                    field = value
                    field.videoGravity = videoGravity
                }
            override var videoGravity: Int
                get() {
                    return strategy.videoGravity
                }
                set(value) {
                    strategy.videoGravity = value
                }

            private var texture: SurfaceTexture? = null
            private var context: GlPixelContext = GlPixelContext()

            override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
                this.context.applicationContext = context.applicationContext
                this.context.setUp()
                this.context.eglContext = EGL14.eglGetCurrentContext()
                texture = this.context.createSurfaceTexture(640, 480)
                strategy.context = this.context
                (stream?.video as CameraSource).surface = Surface(texture)
                strategy.onSurfaceCreated(gl, config)
            }

            override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "${this::onSurfaceChanged.name}: $strategy")
                }
                strategy.onSurfaceChanged(gl, width, height)
            }

            override fun onDrawFrame(gl: GL10) {
                this@GlHkView.stream?.videoCodec?.context = this.context
                texture?.updateTexImage()
                texture?.let {
                    stream?.videoCodec?.frameAvailable(it)
                }
                strategy.onDrawFrame(gl)
            }
        }
        renderer.videoGravity = videoGravity
        renderer
    }

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
    }

    override fun startRunning() {
        if (isRunning.get()) return

        renderer.strategy = stream?.video?.createGLSurfaceViewRenderer() ?: VideoSource.NullRenderer.instance
        isRunning.set(true)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, this::startRunning.name)
        }
    }

    override fun stopRunning() {
        if (!isRunning.get()) return

        renderer.strategy = VideoSource.NullRenderer.instance
        isRunning.set(false)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, this::stopRunning.name)
        }
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    companion object {
        private val TAG = GlHkView::class.java.simpleName
    }
}
