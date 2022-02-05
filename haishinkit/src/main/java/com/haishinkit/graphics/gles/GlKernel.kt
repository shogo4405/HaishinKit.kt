package com.haishinkit.graphics.gles

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import android.util.Size
import android.view.Surface
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.ResampleFilter
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.lang.Utilize
import com.haishinkit.util.aspectRatio
import com.haishinkit.util.swap
import javax.microedition.khronos.opengles.GL10

internal class GlKernel(
    override var utilizable: Boolean = false
) : Utilize {
    var surface: Surface? = null
        set(value) {
            field = value
            if (value == null) {
                tearDown()
            } else {
                setUp()
            }
        }
    var imageOrientation: ImageOrientation = ImageOrientation.UP
        set(value) {
            field = value
            invalidateLayout = true
        }
    var videoGravity: VideoGravity = VideoGravity.RESIZE_ASPECT_FILL
        set(value) {
            field = value
            invalidateLayout = true
        }
    var extent: Size = Size(0, 0)
        set(value) {
            field = value
            invalidateLayout = true
        }
    var resampleFilter: ResampleFilter = ResampleFilter.NEAREST
    var surfaceOrientation: Int = Surface.ROTATION_0
        set(value) {
            field = value
            invalidateLayout = true
        }
    private val inputSurfaceWindow: GlWindowSurface = GlWindowSurface()
    private val vertexBuffer = GlUtil.createFloatBuffer(VERTECES)
    private val texCoordBuffer = GlUtil.createFloatBuffer(TEX_COORDS_ROTATION_0)
    private var program = INVALID_VALUE
    private var positionHandle = INVALID_VALUE
    private var texCoordHandle = INVALID_VALUE
    private var textureHandle = INVALID_VALUE
    private var invalidateLayout = true

    override fun setUp() {
        if (utilizable) return

        inputSurfaceWindow.setUp(surface, null)
        inputSurfaceWindow.makeCurrent()

        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER,
            resampleFilter.glValue
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER,
            resampleFilter.glValue
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S,
            GL10.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_CLAMP_TO_EDGE
        )

        program = GlUtil.createProgram(GlShader.VERTEX, GlShader.FRAGMENT)
        positionHandle = GLES20.glGetAttribLocation(program, "position")
        GLES20.glEnableVertexAttribArray(positionHandle)
        texCoordHandle = GLES20.glGetAttribLocation(program, "texcoord")
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        textureHandle = GLES20.glGetAttribLocation(program, "texture")

        utilizable = true
    }

    override fun tearDown() {
        if (!utilizable) return

        inputSurfaceWindow.tearDown()

        program = INVALID_VALUE
        positionHandle = INVALID_VALUE
        texCoordHandle = INVALID_VALUE
        texCoordHandle = INVALID_VALUE

        utilizable = false
    }

    fun render(textureId: Int, textureSize: Size, timestamp: Long) {
        if (invalidateLayout) {
            layout(textureSize)
            invalidateLayout = false
        }

        GLES20.glUseProgram(program)

        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GlUtil.checkGlError("glVertexAttribPointer")

        GLES20.glUniform1i(textureHandle, 0)
        GlUtil.checkGlError("glUniform1i")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GlUtil.checkGlError("glBindTexture")

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glUseProgram(0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

        inputSurfaceWindow.swapBuffers()
    }

    private fun layout(newTextureSize: Size) {
        var swapped = false

        if (extent.width < extent.height) {
            // portrait
            when (imageOrientation) {
                ImageOrientation.UP, ImageOrientation.UP_MIRRORED -> {
                    texCoordBuffer.put(TEX_COORDS_ROTATION_0)
                }
                ImageOrientation.LEFT, ImageOrientation.LEFT_MIRRORED -> {
                    swapped = true
                    texCoordBuffer.put(TEX_COORDS_ROTATION_270)
                }
                ImageOrientation.DOWN, ImageOrientation.DOWN_MIRRORED -> {
                    texCoordBuffer.put(TEX_COORDS_ROTATION_180)
                }
                ImageOrientation.RIGHT, ImageOrientation.RIGHT_MIRRORED -> {
                    swapped = true
                    texCoordBuffer.put(TEX_COORDS_ROTATION_90)
                }
            }
        } else {
            // landscape
            when (imageOrientation) {
                ImageOrientation.UP, ImageOrientation.UP_MIRRORED -> {
                    swapped = true
                    texCoordBuffer.put(TEX_COORDS_ROTATION_270)
                }
                ImageOrientation.LEFT, ImageOrientation.LEFT_MIRRORED -> {
                    if (surfaceOrientation == Surface.ROTATION_270) {
                        texCoordBuffer.put(TEX_COORDS_ROTATION_180)
                    } else {
                        texCoordBuffer.put(TEX_COORDS_ROTATION_0)
                    }
                }
                ImageOrientation.DOWN, ImageOrientation.DOWN_MIRRORED -> {
                    swapped = true
                    texCoordBuffer.put(TEX_COORDS_ROTATION_90)
                }
                ImageOrientation.RIGHT, ImageOrientation.RIGHT_MIRRORED -> {
                    if (surfaceOrientation == Surface.ROTATION_270) {
                        texCoordBuffer.put(TEX_COORDS_ROTATION_180)
                    } else {
                        texCoordBuffer.put(TEX_COORDS_ROTATION_0)
                    }
                }
            }
        }

        texCoordBuffer.position(0)
        val textureSize = newTextureSize.swap(swapped)

        when (videoGravity) {
            VideoGravity.RESIZE_ASPECT -> {
                val xRatio = extent.width.toFloat() / textureSize.width.toFloat()
                val yRatio = extent.height.toFloat() / textureSize.height.toFloat()
                if (yRatio < xRatio) {
                    GLES20.glViewport(
                        ((extent.width - textureSize.width * yRatio) / 2).toInt(),
                        0,
                        (textureSize.width * yRatio).toInt(),
                        extent.height
                    )
                } else {
                    GLES20.glViewport(
                        0,
                        ((extent.height - textureSize.height * xRatio) / 2).toInt(),
                        extent.width,
                        (textureSize.height * xRatio).toInt()
                    )
                }
            }
            VideoGravity.RESIZE_ASPECT_FILL -> {
                val iRatio = extent.aspectRatio
                val fRatio = textureSize.aspectRatio
                if (iRatio < fRatio) {
                    GLES20.glViewport(
                        ((extent.width - extent.height * fRatio) / 2).toInt(),
                        0,
                        (extent.height * fRatio).toInt(),
                        extent.height
                    )
                } else {
                    GLES20.glViewport(
                        0,
                        ((extent.height - extent.width / fRatio) / 2).toInt(),
                        extent.width,
                        (extent.width / fRatio).toInt()
                    )
                }
            }
        }
    }

    companion object {
        private const val INVALID_VALUE = 0

        private val VERTECES = floatArrayOf(
            -1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, -1.0f, 0.0f
        )
        private val TEX_COORDS_ROTATION_0 = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
        )
        private val TEX_COORDS_ROTATION_90 = floatArrayOf(
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
        )
        private val TEX_COORDS_ROTATION_180 = floatArrayOf(
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f
        )
        private val TEX_COORDS_ROTATION_270 = floatArrayOf(
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
        )
    }
}
