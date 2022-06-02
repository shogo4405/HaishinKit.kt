package com.haishinkit.graphics.gles

import android.content.res.AssetManager
import android.opengl.GLES20
import android.util.Log
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.graphics.glsl.Uniform
import java.io.FileNotFoundException
import java.lang.reflect.Method

internal class ShaderLoader {
    var assetManager: AssetManager? = null

    fun createProgram(videoEffect: VideoEffect): Program? {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, videoEffect.name)
        if (vertexShader == 0) {
            return null
        }
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, videoEffect.name)
        if (fragmentShader == 0) {
            return null
        }
        var program = GLES20.glCreateProgram()
        Util.checkGlError(GL_CREATE_PROGRAM)
        if (program == 0) {
            Log.e(TAG, "Could not create program")
        }
        GLES20.glAttachShader(program, vertexShader)
        Util.checkGlError(GL_ATTACH_SHADER)
        GLES20.glAttachShader(program, fragmentShader)
        Util.checkGlError(GL_ATTACH_SHADER)
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ")
            Log.e(TAG, GLES20.glGetProgramInfoLog(program))
            GLES20.glDeleteProgram(program)
            program = 0
        }
        return Program(
            program,
            vertexShader,
            fragmentShader,
            GLES20.glGetAttribLocation(program, "aPosition"),
            GLES20.glGetAttribLocation(program, "aTexcoord"),
            GLES20.glGetAttribLocation(program, "uTexture"),
            GLES20.glGetUniformLocation(program, "uMVPMatrix"),
            handlers(program, videoEffect)
        )
    }

    private fun handlers(program: Int, videoEffect: VideoEffect): Map<Int, Method> {
        val clazz = videoEffect::class.java
        val layouts = mutableMapOf<Uniform, Method>()
        for (method in clazz.methods) {
            val uniform = method.getAnnotation(Uniform::class.java) ?: continue
            layouts[uniform] = clazz.getDeclaredMethod(method.name.split("$")[0])
        }
        val locations = mutableMapOf<Uniform, Int>()
        for (layout in layouts) {
            locations[layout.key] = GLES20.glGetUniformLocation(program, layout.key.name)
        }
        val handlers = mutableMapOf<Int, Method>()
        for (location in locations) {
            layouts[location.key]?.let {
                handlers[location.value] = it
            }
        }
        return handlers
    }

    private fun readFile(shaderType: Int, source: String?): String {
        var fileName = "shaders/" + (source ?: "default.")
        fileName += if (shaderType == GLES20.GL_VERTEX_SHADER) {
            ".vert"
        } else {
            ".frag"
        }
        try {
            val inputStream = assetManager?.open(fileName) ?: return ""
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            return String(buffer)
        } catch (e: FileNotFoundException) {
            return readFile(shaderType, "default")
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            return ""
        }
    }

    private fun loadShader(shaderType: Int, source: String?): Int {
        var shader = GLES20.glCreateShader(shaderType)
        Util.checkGlError("glCreateShader type=$shaderType")
        GLES20.glShaderSource(shader, readFile(shaderType, source))
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader $shaderType:")
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            shader = 0
        }
        return shader
    }

    companion object {
        private val TAG = ShaderLoader::class.java.simpleName

        private const val GL_CREATE_PROGRAM = "glCreateProgram"
        private const val GL_ATTACH_SHADER = "glAttachShader"
    }
}
