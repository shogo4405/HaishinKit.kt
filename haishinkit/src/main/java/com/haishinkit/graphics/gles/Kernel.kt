package com.haishinkit.graphics.gles

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLExt
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import android.util.Size
import android.view.Surface
import com.haishinkit.BuildConfig
import com.haishinkit.graphics.ImageOrientation
import com.haishinkit.graphics.ResampleFilter
import com.haishinkit.graphics.VideoGravity
import com.haishinkit.graphics.effect.DefaultVideoEffect
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.lang.Utilize
import com.haishinkit.util.aspectRatio
import com.haishinkit.util.swap
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.opengles.GL10

internal class Kernel(
    override var utilizable: Boolean = false
) : Utilize {
    var outputSurface: Surface? = null
        set(value) {
            field = value
            inputSurfaceWindow.setSurface(value)
        }

    var imageOrientation = ImageOrientation.UP
        set(value) {
            field = value
            invalidateLayout = true
        }

    var videoGravity = VideoGravity.RESIZE_ASPECT_FILL
        set(value) {
            field = value
            invalidateLayout = true
        }

    var imageExtent = Size(0, 0)
        set(value) {
            field = value
            GLES20.glViewport(
                0,
                0,
                imageExtent.width,
                imageExtent.height
            )
            invalidateLayout = true
        }

    var resampleFilter = ResampleFilter.NEAREST

    var deviceOrientation: Int = Surface.ROTATION_0
        set(value) {
            field = value
            invalidateLayout = true
        }

    var isRotatesWithContent = true

    var videoEffect: VideoEffect = DefaultVideoEffect.shared
        set(value) {
            field = value
            program = shaderLoader.createProgram(videoEffect)
        }

    var assetManager: AssetManager? = null
        set(value) {
            field = value
            shaderLoader.assetManager = assetManager
            program = shaderLoader.createProgram(videoEffect)
        }

    var version: Int = 0
        private set

    private val matrix = FloatArray(16)
    private val inputSurfaceWindow = WindowSurface()
    private val vertexBuffer = Util.createFloatBuffer(VERTECES)
    private val texCoordBuffer = Util.createFloatBuffer(TEX_COORDS_ROTATION_0)
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
        val shaderLoader = ShaderLoader()
        shaderLoader.assetManager = assetManager
        shaderLoader
    }

    private var program: Program? = null
        set(value) {
            field?.dispose()
            field = value
        }

    override fun setUp() {
        if (utilizable) return

        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display === EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("unable to get EGL14 display")
        }

        val version = IntArray(2)
        if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
            throw RuntimeException("unable to initialize EGL14")
        }

        var config = chooseConfig(3)
        if (config != null) {
            val context = EGL14.eglCreateContext(
                display,
                config,
                EGL14.EGL_NO_CONTEXT,
                intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE),
                0
            )
            if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                this.context = context
                this.version = 3
            }
        }

        // fall back to 2.0
        if (context == EGL14.EGL_NO_CONTEXT) {
            config = chooseConfig(2)
            if (config == null) {
                throw RuntimeException("unable to find a suitable EGLConfig")
            }
            context = EGL14.eglCreateContext(
                display,
                config,
                EGL14.EGL_NO_CONTEXT,
                intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE),
                0
            )
            this.version = 2
        }

        inputSurfaceWindow.config = config
        Util.checkGlError("eglCreateContext")
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

        program.bind(videoEffect)

        GLES20.glUniformMatrix4fv(program.mvpMatrixHandle, 1, false, matrix, 0)
        Util.checkGlError("glUniformMatrix4fv")

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
        Util.checkGlError("glVertexAttribPointer")

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUniform1i(program.textureHandle, 0)
        Util.checkGlError("glUniform1i")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        Util.checkGlError("glBindTexture")

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glUseProgram(0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

        inputSurfaceWindow.setPresentationTime(timestamp)
        inputSurfaceWindow.swapBuffers()
    }

    fun readPixels(): Bitmap? {
        if (outputSurface == null) {
            return null
        }
        val bitmap =
            Bitmap.createBitmap(imageExtent.width, imageExtent.height, Bitmap.Config.ARGB_8888)
        val byteBuffer =
            ByteBuffer.allocateDirect(imageExtent.width * imageExtent.height * 4).apply {
                order(ByteOrder.LITTLE_ENDIAN)
            }
        inputSurfaceWindow.readPixels(imageExtent.width, imageExtent.height, byteBuffer)
        bitmap.copyPixelsFromBuffer(byteBuffer)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            imageExtent.width,
            imageExtent.height,
            Matrix().apply {
                setRotate(
                    180.0F
                )
            },
            false
        )
    }

    fun invalidateLayout() {
        invalidateLayout = true
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
                matrix[0] = 1f
                matrix[5] = 1f
                matrix[10] = 1f
                matrix[15] = 1f
            }

            VideoGravity.RESIZE_ASPECT -> {
                val iRatio = imageExtent.aspectRatio
                val fRatio = textureSize.aspectRatio
                if (iRatio < fRatio) {
                    matrix[0] = 1f
                    matrix[5] = textureSize.height.toFloat() / textureSize.width.toFloat() * iRatio
                    matrix[10] = 1f
                    matrix[15] = 1f
                } else {
                    matrix[0] = textureSize.width.toFloat() / textureSize.height.toFloat() / iRatio
                    matrix[5] = 1f // y
                    matrix[10] = 1f
                    matrix[15] = 1f
                }
            }

            VideoGravity.RESIZE_ASPECT_FILL -> {
                val iRatio = imageExtent.aspectRatio
                val fRatio = textureSize.aspectRatio
                if (iRatio < fRatio) {
                    matrix[0] = imageExtent.height.toFloat() / imageExtent.width.toFloat() * fRatio
                    matrix[5] = 1f
                    matrix[10] = 1f
                    matrix[15] = 1f
                } else {
                    matrix[0] = 1f
                    matrix[5] = imageExtent.width.toFloat() / imageExtent.height.toFloat() / fRatio
                    matrix[10] = 1f
                    matrix[15] = 1f
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Log.i(TAG, matrix.contentToString())
        }
    }

    private fun chooseConfig(version: Int): EGLConfig? {
        var renderableType = EGL14.EGL_OPENGL_ES2_BIT

        if (2 < version) {
            renderableType = renderableType or EGLExt.EGL_OPENGL_ES3_BIT_KHR
        }

        val attributes = CONFIG_ATTRIBUTES_WITH_CONTEXT
        attributes[9] = renderableType

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
            Log.e(TAG, "unable to find RGB8888 EGLConfig($version)")
            return null
        }

        return configs[0]
    }

    companion object {
        private val TAG = Kernel::class.java.toString()
        private const val EGL_RECORDABLE_ANDROID: Int = 0x3142

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
            -1.0f, 1.0f, 0.0f, // top-left
            -1.0f, -1.0f, 0.0f, // bottom-left
            1.0f, 1.0f, 0.0f, // bottom-right
            1.0f, -1.0f, 0.0f // top-right
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
