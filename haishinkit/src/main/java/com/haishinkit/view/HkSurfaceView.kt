package com.haishinkit.view

import android.content.Context
import android.util.AttributeSet
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import com.haishinkit.graphics.PixelTransform
import com.haishinkit.graphics.PixelTransformFactory
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.net.NetStream

@Suppress("unused")
class HkSurfaceView(context: Context, attributes: AttributeSet) :
    SurfaceView(context, attributes),
    HkView {
    override var videoGravity: VideoGravity
        get() = pixelTransform.videoGravity
        set(value) {
            pixelTransform.videoGravity = value
        }
    override var stream: NetStream? = null
        set(value) {
            field?.renderer = null
            field = value
            field?.renderer = this
        }
    override val pixelTransform: PixelTransform by lazy {
        PixelTransformFactory().create()
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

    companion object {
        private var TAG = HkSurfaceView::class.java.simpleName
    }
}
