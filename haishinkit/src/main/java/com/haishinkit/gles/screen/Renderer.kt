package com.haishinkit.gles.screen

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import android.view.Surface
import com.haishinkit.BuildConfig
import com.haishinkit.gles.Program
import com.haishinkit.gles.ShaderLoader
import com.haishinkit.gles.Utils
import com.haishinkit.graphics.effect.DefaultVideoEffect
import com.haishinkit.screen.Renderer
import com.haishinkit.screen.ScreenObject
import com.haishinkit.screen.Video
import javax.microedition.khronos.opengles.GL10

internal class Renderer(applicationContext: Context) :
    Renderer,
    SurfaceTexture.OnFrameAvailableListener {
    private var programs = mutableMapOf<Int, Program>()
    private var textureIds = intArrayOf(0)
    private var surfaceTextures = mutableMapOf<Int, SurfaceTexture>()
    private val shaderLoader by lazy {
        ShaderLoader(applicationContext)
    }

    init {
        shaderLoader.createProgram(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            DefaultVideoEffect.shared,
        )?.let {
            programs[GLES11Ext.GL_TEXTURE_EXTERNAL_OES] = it
        }
        shaderLoader.createProgram(
            GLES20.GL_TEXTURE_2D,
            DefaultVideoEffect.shared,
        )?.let {
            programs[GLES20.GL_TEXTURE_2D] = it
        }
    }

    override fun layout(screenObject: ScreenObject) {
        when (screenObject) {
            is Video -> {
                surfaceTextures[screenObject.id]?.setDefaultBufferSize(
                    screenObject.videoSize.width,
                    screenObject.videoSize.height,
                )
                GLES20.glTexParameteri(
                    screenObject.target,
                    GL10.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST,
                )
                GLES20.glTexParameteri(
                    screenObject.target,
                    GL10.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR,
                )
                GLES20.glTexParameteri(
                    screenObject.target,
                    GL10.GL_TEXTURE_WRAP_S,
                    GL10.GL_CLAMP_TO_EDGE,
                )
                GLES20.glTexParameteri(
                    screenObject.target,
                    GL10.GL_TEXTURE_WRAP_T,
                    GL10.GL_CLAMP_TO_EDGE,
                )
            }

            is com.haishinkit.screen.Image -> {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, screenObject.id)
                Utils.checkGlError("glBindTexture")
                GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1)
                Utils.checkGlError("glPixelStorei")
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, screenObject.bitmap, 0)
                GLES20.glTexParameteri(
                    screenObject.target,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST,
                )
                GLES20.glTexParameteri(
                    screenObject.target,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR,
                )
                GLES20.glTexParameteri(
                    screenObject.target,
                    GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE,
                )
                GLES20.glTexParameteri(
                    screenObject.target,
                    GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE,
                )
            }
        }
    }

    override fun draw(screenObject: ScreenObject) {
        val program = programs[screenObject.target] ?: return
        GLES20.glViewport(
            screenObject.x,
            screenObject.y,
            screenObject.width,
            screenObject.height,
        )
        program.bind(DefaultVideoEffect.shared)
        program.draw(screenObject)
    }

    override fun bind(screenObject: ScreenObject) {
        GLES20.glGenTextures(1, textureIds, 0)
        screenObject.id = textureIds[0]
        when (screenObject) {
            is Video -> {
                SurfaceTexture(screenObject.id).apply {
                    surfaceTextures[screenObject.id] = this
                    setDefaultBufferSize(
                        screenObject.videoSize.width,
                        screenObject.videoSize.height,
                    )
                    screenObject.surface = Surface(this)
                    setOnFrameAvailableListener(this@Renderer)
                }
            }
        }
    }

    override fun unbind(screenObject: ScreenObject) {
        textureIds[0] = screenObject.id
        when (screenObject) {
            is Video -> {
                screenObject.surface = null
                surfaceTextures[screenObject.id]?.setOnFrameAvailableListener(null)
                surfaceTextures[screenObject.id]?.release()
                surfaceTextures.remove(screenObject.id)
            }
        }
        GLES20.glDeleteTextures(1, textureIds, 0)
        screenObject.id = 0
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        try {
            surfaceTexture?.updateTexImage()
        } catch (e: RuntimeException) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "", e)
            }
        }
    }

    companion object {
        private val TAG = Renderer::class.java.simpleName
    }
}