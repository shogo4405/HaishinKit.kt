package com.haishinkit.view

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.PixelTransformFactory
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.net.NetStream

/**
 * A view that displays a video content of a NetStream object which uses [SurfaceView].
 */
class HkSurfaceView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : SurfaceView(context, attrs, defStyleAttr, defStyleRes), NetStreamDrawable {

    override var videoGravity: VideoGravity
        get() = pixelTransform.videoGravity
        set(value) {
            pixelTransform.videoGravity = value
        }

    override var frameRate: Int
        get() = pixelTransform.frameRate
        set(value) {
            pixelTransform.frameRate = value
        }

    override var imageOrientation: ImageOrientation
        get() = pixelTransform.imageOrientation
        set(value) {
            pixelTransform.imageOrientation = value
        }

    override var videoEffect: VideoEffect
        get() = pixelTransform.videoEffect
        set(value) {
            pixelTransform.videoEffect = value
        }

    override var isRotatesWithContent: Boolean
        get() = pixelTransform.isRotatesWithContent
        set(value) {
            pixelTransform.isRotatesWithContent = value
        }

    private val pixelTransform: PixelTransform by lazy {
        PixelTransformFactory().create()
    }

    private var stream: NetStream? = null
        set(value) {
            field?.drawable = null
            field = value
            field?.drawable = this
        }

    init {
        pixelTransform.assetManager = context.assets

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                pixelTransform.imageExtent = Size(width, height)
                pixelTransform.outputSurface = holder.surface
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                pixelTransform.imageExtent = Size(width, height)
                (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.orientation?.let {
                    pixelTransform.deviceOrientation = it
                    stream?.videoCodec?.pixelTransform?.deviceOrientation = it
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                pixelTransform.outputSurface = null
            }
        })
    }

    override fun attachStream(stream: NetStream?) {
        this.stream = stream
    }

    override fun readPixels(lambda: (bitmap: Bitmap?) -> Unit) {
        pixelTransform.readPixels(lambda)
    }

    override fun createInputSurface(
        width: Int,
        height: Int,
        format: Int,
        lambda: ((surface: Surface) -> Unit)
    ) {
        pixelTransform.createInputSurface(width, height, format, lambda)
    }

    override fun dispose() {
        pixelTransform.dispose()
    }

    companion object {
        private var TAG = HkSurfaceView::class.java.simpleName
    }
}
