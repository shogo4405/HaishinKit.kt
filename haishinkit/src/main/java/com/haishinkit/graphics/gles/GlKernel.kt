package com.haishinkit.graphics.gles

import android.content.res.AssetManager
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Size
import android.view.Surface
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.ResampleFilter
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.filter.DefaultVideoEffect
import com.haishinkit.graphics.filter.VideoEffect
import com.haishinkit.lang.Utilize
import com.haishinkit.util.aspectRatio
import com.haishinkit.util.swap
import javax.microedition.khronos.opengles.GL10

internal class GlKernel(
    override var utilizable: Boolean = false
) : Utilize {
    var outputSurface: Surface? = null
        set(value) {
            field = value
            inputSurfaceWindow.setSurface(value)
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

    var imageExtent: Size = Size(0, 0)
        set(value) {
            field = value
            invalidateLayout = true
        }

    var resampleFilter: ResampleFilter = ResampleFilter.NEAREST

    var deviceOrientation: Int = Surface.ROTATION_0
        set(value) {
            field = value
            invalidateLayout = true
        }

    var isRotatesWithContent = true

    var videoEffect: VideoEffect = DefaultVideoEffect()
        set(value) {
            field = value
            program = shaderLoader.createProgram(videoEffect.name)
        }

    var assetManager: AssetManager? = null
        set(value) {
            field = value
            shaderLoader.assetManager = assetManager
            program = shaderLoader.createProgram(videoEffect.name)
        }

    private val inputSurfaceWindow: GlWindowSurface = GlWindowSurface()
    private val vertexBuffer = GlUtil.createFloatBuffer(VERTECES)
    private val texCoordBuffer = GlUtil.createFloatBuffer(TEX_COORDS_ROTATION_0)
    private var invalidateLayout = true
    private var display = EGL14.EGL_NO_DISPLAY
        set(value) {
            field = value
            inputSurfaceWindow.display = value
        }

    private var context = EGL14.EGL_NO_CONTEXT
        set(value) {
            field = value
            inputSurfaceWindow.context = value
        }

    private val shaderLoader by lazy {
        val shaderLoader = GlShaderLoader()
        shaderLoader.assetManager = assetManager
        shaderLoader
    }

    private var program: GlShaderLoader.Program? = null
        set(value) {
            field?.dispose()
            field = value
        }

    override fun setUp() {
        if (utilizable) return

        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display === EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException()
        }

        val version = IntArray(2)
        if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
            throw RuntimeException()
        }

        val config = chooseConfig() ?: return
        inputSurfaceWindow.config = config
        context = EGL14.eglCreateContext(
            display,
            config,
            EGL14.EGL_NO_CONTEXT,
            CONTEXT_ATTRIBUTES,
            0
        )
        GlUtil.checkGlError("eglCreateContext")
        EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, context)

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

        utilizable = true
    }

    override fun tearDown() {
        if (!utilizable) return
        program = null
        utilizable = false
    }

    fun render(textureId: Int, textureSize: Size, timestamp: Long) {
        if (invalidateLayout) {
            layout(textureSize)
            invalidateLayout = false
        }

        val program = program ?: return

        GLES20.glUseProgram(program.id)

        GLES20.glVertexAttribPointer(
            program.texCoordHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            texCoordBuffer
        )
        GLES20.glVertexAttribPointer(
            program.positionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            0,
            vertexBuffer
        )
        GlUtil.checkGlError("glVertexAttribPointer")

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUniform1i(program.textureHandle, 0)
        GlUtil.checkGlError("glUniform1i")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GlUtil.checkGlError("glBindTexture")

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glUseProgram(0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

        inputSurfaceWindow.setPresentationTime(timestamp)
        inputSurfaceWindow.swapBuffers()
    }

    private fun layout(newTextureSize: Size) {
        var degrees = when (imageOrientation) {
            ImageOrientation.UP -> 0
            ImageOrientation.DOWN -> 180
            ImageOrientation.LEFT -> 270
            ImageOrientation.RIGHT -> 90
            ImageOrientation.UP_MIRRORED -> 0
            ImageOrientation.DOWN_MIRRORED -> 180
            ImageOrientation.LEFT_MIRRORED -> 270
            ImageOrientation.RIGHT_MIRRORED -> 90
        }

        if (isRotatesWithContent) {
            degrees += when (deviceOrientation) {
                0 -> 0
                1 -> 90
                2 -> 180
                3 -> 270
                else -> 0
            }
        }

        if (degrees.rem(180) == 0 && (imageOrientation == ImageOrientation.RIGHT || imageOrientation == ImageOrientation.RIGHT_MIRRORED)) {
            degrees += 180
        }

        var swapped = false
        when (degrees.rem(360)) {
            0 -> {
                texCoordBuffer.put(TEX_COORDS_ROTATION_0)
            }
            90 -> {
                swapped = true
                texCoordBuffer.put(TEX_COORDS_ROTATION_90)
            }
            180 -> {
                texCoordBuffer.put(TEX_COORDS_ROTATION_180)
            }
            270 -> {
                swapped = true
                texCoordBuffer.put(TEX_COORDS_ROTATION_270)
            }
        }

        texCoordBuffer.position(0)
        val textureSize = newTextureSize.swap(swapped)

        when (videoGravity) {
            VideoGravity.RESIZE -> {
                GLES20.glViewport(
                    0,
                    0,
                    imageExtent.width,
                    imageExtent.height
                )
            }
            VideoGravity.RESIZE_ASPECT -> {
                val xRatio = imageExtent.width.toFloat() / textureSize.width.toFloat()
                val yRatio = imageExtent.height.toFloat() / textureSize.height.toFloat()
                if (yRatio < xRatio) {
                    GLES20.glViewport(
                        ((imageExtent.width - textureSize.width * yRatio) / 2).toInt(),
                        0,
                        (textureSize.width * yRatio).toInt(),
                        imageExtent.height
                    )
                } else {
                    GLES20.glViewport(
                        0,
                        ((imageExtent.height - textureSize.height * xRatio) / 2).toInt(),
                        imageExtent.width,
                        (textureSize.height * xRatio).toInt()
                    )
                }
            }
            VideoGravity.RESIZE_ASPECT_FILL -> {
                val iRatio = imageExtent.aspectRatio
                val fRatio = textureSize.aspectRatio
                if (iRatio < fRatio) {
                    GLES20.glViewport(
                        ((imageExtent.width - imageExtent.height * fRatio) / 2).toInt(),
                        0,
                        (imageExtent.height * fRatio).toInt(),
                        imageExtent.height
                    )
                } else {
                    GLES20.glViewport(
                        0,
                        ((imageExtent.height - imageExtent.width / fRatio) / 2).toInt(),
                        imageExtent.width,
                        (imageExtent.width / fRatio).toInt()
                    )
                }
            }
        }
    }

    private fun chooseConfig(): EGLConfig? {
        val attributes: IntArray = CONFIG_ATTRIBUTES_WITH_CONTEXT
        val configs: Array<EGLConfig?> = arrayOfNulls(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(
                display,
                attributes,
                0,
                configs,
                0,
                configs.size,
                numConfigs,
                0
            )
        ) {
            return null
        }
        return configs[0]
    }

    companion object {
        private const val EGL_RECORDABLE_ANDROID: Int = 0x3142

        private val CONTEXT_ATTRIBUTES =
            intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)

        private val CONFIG_ATTRIBUTES_WITH_CONTEXT = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL_RECORDABLE_ANDROID, 1,
            EGL14.EGL_NONE
        )

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
