package com.haishinkit.screen

import android.opengl.GLES11Ext
import android.opengl.Matrix
import android.util.Size
import android.view.Surface
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.util.aspectRatio
import com.haishinkit.util.swap

@Suppress("MemberVisibilityCanBePrivate")
class Video(target: Int = GLES11Ext.GL_TEXTURE_EXTERNAL_OES) : ScreenObject(target) {

    var surface: Surface? = null
        set(value) {
            field = value
            listener?.onSurfaceChanged(value)
        }
    var listener: OnSurfaceChangedListener? = null

    /**
     * Specifies the videoGravity how the displays the visual content.
     */
    var videoGravity: VideoGravity = VideoGravity.RESIZE_ASPECT_FILL
        set(value) {
            field = value
            invalidateLayout()
        }

    /**
     * Specifies the imageOrientation that describe the image orientation.
     */
    var imageOrientation: ImageOrientation = ImageOrientation.UP
        set(value) {
            field = value
            invalidateLayout()
        }

    /**
     * Specifies the videoSize that describe the video source.
     */
    var videoSize = Size(0, 0)
        set(value) {
            field = value
            invalidateLayout()
        }

    /**
     * Specifies whether displayed images rotates(true), or not(false).
     */
    var isRotatesWithContent: Boolean = false
        set(value) {
            field = value
            invalidateLayout()
        }

    override fun layout(renderer: ScreenRenderer) {
        super.layout(renderer)

        var degrees = when (imageOrientation) {
            ImageOrientation.UP -> 0
            ImageOrientation.DOWN -> 180
            ImageOrientation.LEFT -> 90
            ImageOrientation.RIGHT -> 270
            ImageOrientation.UP_MIRRORED -> 0
            ImageOrientation.DOWN_MIRRORED -> 180
            ImageOrientation.LEFT_MIRRORED -> 270
            ImageOrientation.RIGHT_MIRRORED -> 90
        }

        if (isRotatesWithContent) {
            degrees += when (renderer.deviceOrientation) {
                0 -> 0
                1 -> 270
                2 -> 180
                3 -> 90
                else -> 0
            }
        }

        if (degrees.rem(180) == 0 && (imageOrientation == ImageOrientation.RIGHT || imageOrientation == ImageOrientation.RIGHT_MIRRORED)) {
            degrees += 180
        }

        Matrix.setIdentityM(matrix, 0)

        if (target == GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
            matrix[5] = matrix[5] * -1
            Matrix.rotateM(matrix, 0, -degrees.toFloat(), 0f, 0f, 1f)
        }

        val swapped = degrees == 90 || degrees == 270
        val newVideoSize = videoSize.swap(swapped)
        when (videoGravity) {
            VideoGravity.RESIZE -> {
                // no op
            }

            VideoGravity.RESIZE_ASPECT -> {
                var x: Float
                var y: Float
                val iRatio = width.toFloat() / height.toFloat()
                val fRatio = newVideoSize.aspectRatio
                if (iRatio < fRatio) {
                    x = 1f
                    y = newVideoSize.height.toFloat() / newVideoSize.width.toFloat() * iRatio
                    if (swapped) {
                        x = y
                        y = 1f
                    }
                } else {
                    x = newVideoSize.width.toFloat() / newVideoSize.height.toFloat() / iRatio
                    y = 1f
                    if (swapped) {
                        y = x
                        x = 1f
                    }
                }
                Matrix.scaleM(
                    matrix,
                    0,
                    x,
                    y,
                    1f
                )
            }

            VideoGravity.RESIZE_ASPECT_FILL -> {
                var x: Float
                var y: Float
                val iRatio = width.toFloat() / height.toFloat()
                val fRatio = newVideoSize.aspectRatio
                if (iRatio < fRatio) {
                    x = height.toFloat() / width.toFloat() * fRatio
                    y = 1f
                    if (swapped) {
                        y = x
                        x = 1f
                    }
                } else {
                    x = 1f
                    y = width.toFloat() / height.toFloat() / fRatio
                    if (swapped) {
                        x = y
                        y = 1f
                    }
                }
                Matrix.scaleM(
                    matrix,
                    0,
                    x,
                    y,
                    1f
                )
            }
        }
    }

    interface OnSurfaceChangedListener {
        fun onSurfaceChanged(surface: Surface?)
    }

    companion object {
        private val TAG = Video::class.java.simpleName
    }
}
