package com.haishinkit.gles.renderer

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Size
import com.haishinkit.gles.GlPixelContext
import com.haishinkit.gles.GlUtil
import com.haishinkit.util.VideoGravity
import com.haishinkit.util.aspectRatio
import com.haishinkit.util.swap
import javax.microedition.khronos.opengles.GL10

class GlFramePixelRenderer(
    override var utilizable: Boolean = false,
    override var resolution: Size = Size(0, 0),
    override var videoGravity: Int = VideoGravity.RESIZE_ASPECT_FILL
) : GlPixelRenderer {
    private val vertexBuffer = GlUtil.createFloatBuffer(VERTECES)
    private val texCoordBuffer = GlUtil.createFloatBuffer(TEX_COORDS_ROTATION_0)
    private var program = INVALID_VALUE
    private var positionHandle = INVALID_VALUE
    private var texCoordHandle = INVALID_VALUE
    private var textureHandle = INVALID_VALUE

    override fun setUp() {
        if (utilizable) return

        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_LINEAR.toFloat()
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

        program = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        positionHandle = GLES20.glGetAttribLocation(program, "position")
        GLES20.glEnableVertexAttribArray(positionHandle)
        texCoordHandle = GLES20.glGetAttribLocation(program, "texcoord")
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        textureHandle = GLES20.glGetAttribLocation(program, "texture")

        utilizable = true
    }

    override fun tearDown() {
        if (!utilizable) return

        program = INVALID_VALUE
        positionHandle = INVALID_VALUE
        texCoordHandle = INVALID_VALUE
        texCoordHandle = INVALID_VALUE

        utilizable = false
    }

    override fun render(context: GlPixelContext, matrix: FloatArray) {
        configuration(context)

        GLES20.glUseProgram(program)

        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GlUtil.checkGlError("glVertexAttribPointer")

        GLES20.glUniform1i(textureHandle, 0)
        GlUtil.checkGlError("glUniform1i")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, context.textureId)
        GlUtil.checkGlError("glBindTexture")

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glUseProgram(0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
    }

    private fun configuration(context: GlPixelContext) {
        var swapped = false
        when (context.orientation) {
            GlPixelContext.ROTATION_0 -> {
                texCoordBuffer.put(TEX_COORDS_ROTATION_0)
            }
            GlPixelContext.ROTATION_90 -> {
                swapped = true
                texCoordBuffer.put(TEX_COORDS_ROTATION_90)
            }
            GlPixelContext.ROTATION_180 -> {
                texCoordBuffer.put(TEX_COORDS_ROTATION_180)
            }
            GlPixelContext.ROTATION_270 -> {
                swapped = true
                texCoordBuffer.put(TEX_COORDS_ROTATION_270)
            }
        }
        texCoordBuffer.position(0)

        val textureSize = context.textureSize.swap(swapped)

        when (videoGravity) {
            VideoGravity.RESIZE_ASPECT -> {
                val xRatio = resolution.width.toFloat() / textureSize.width.toFloat()
                val yRatio = resolution.height.toFloat() / textureSize.height.toFloat()
                if (yRatio < xRatio) {
                    GLES20.glViewport(
                        ((resolution.width - textureSize.width * yRatio) / 2).toInt(),
                        0,
                        (textureSize.width * yRatio).toInt(),
                        resolution.height
                    )
                } else {
                    GLES20.glViewport(
                        0,
                        ((resolution.height - textureSize.height * xRatio) / 2).toInt(),
                        resolution.width,
                        (textureSize.height * xRatio).toInt()
                    )
                }
            }
            VideoGravity.RESIZE_ASPECT_FILL -> {
                val iRatio = resolution.aspectRatio
                val fRatio = textureSize.aspectRatio
                if (iRatio < fRatio) {
                    GLES20.glViewport(
                        ((resolution.width - resolution.height * fRatio) / 2).toInt(),
                        0,
                        (resolution.height * fRatio).toInt(),
                        resolution.height
                    )
                } else {
                    GLES20.glViewport(
                        0,
                        ((resolution.height - resolution.width / fRatio) / 2).toInt(),
                        resolution.width,
                        (resolution.width / fRatio).toInt()
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
        private const val VERTEX_SHADER = "attribute vec4 position;\n" +
            "attribute vec2 texcoord;\n" +
            "varying vec2 texcoordVarying;\n" +
            "void main() {\n" +
            "    gl_Position = position;\n" +
            "    texcoordVarying = texcoord;\n" +
            "}\n"
        private const val FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 texcoordVarying;\n" +
            "uniform samplerExternalOES texture;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(texture, texcoordVarying);\n" +
            "}\n"

        private val TAG = GlFramePixelRenderer::class.java.simpleName
    }
}
