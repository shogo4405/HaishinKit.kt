package com.haishinkit.graphics.gles

import android.content.res.AssetManager
import android.opengl.GLES20
import android.util.Log
import java.io.FileNotFoundException

internal class GlShaderLoader {
    var assetManager: AssetManager? = null

    data class Program(
        val id: Int = INVALID_VALUE,
        val vertexShader: Int = INVALID_VALUE,
        val fragmentShaper: Int = INVALID_VALUE,
        val positionHandle: Int = INVALID_VALUE,
        val texCoordHandle: Int = INVALID_VALUE,
        val textureHandle: Int = INVALID_VALUE
    ) {
        init {
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glEnableVertexAttribArray(texCoordHandle)
        }

        fun dispose() {
            GLES20.glDetachShader(id, vertexShader)
            GLES20.glDeleteShader(vertexShader)
            GLES20.glDetachShader(id, fragmentShaper)
            GLES20.glDeleteShader(fragmentShaper)
            GLES20.glDeleteProgram(id)
        }
    }

    fun createProgram(name: String): Program? {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, name)
        if (vertexShader == 0) {
            return null
        }
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, name)
        if (fragmentShader == 0) {
            return null
        }
        var program = GLES20.glCreateProgram()
        GlUtil.checkGlError(GL_CREATE_PROGRAM)
        if (program == 0) {
            Log.e(TAG, "Could not create program")
        }
        GLES20.glAttachShader(program, vertexShader)
        GlUtil.checkGlError(GL_ATTACH_SHADER)
        GLES20.glAttachShader(program, fragmentShader)
        GlUtil.checkGlError(GL_ATTACH_SHADER)
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
            GLES20.glGetAttribLocation(program, "position"),
            GLES20.glGetAttribLocation(program, "texcoord"),
            GLES20.glGetAttribLocation(program, "texture")
        )
    }

    private fun readFile(shaderType: Int, source: String?): String {
        var fileName = "shaders/" + (source ?: "main.")
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
        GlUtil.checkGlError("glCreateShader type=$shaderType")
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
        private const val INVALID_VALUE = 0
        private val TAG = GlShaderLoader::class.java.simpleName

        private const val GL_CREATE_PROGRAM = "glCreateProgram"
        private const val GL_ATTACH_SHADER = "glAttachShader"
    }
}
