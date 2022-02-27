package com.haishinkit.graphics.gles

import android.content.res.AssetManager
import android.opengl.GLES20
import android.util.Log
import java.io.FileNotFoundException
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

internal object GlShaderLoader {
    val TAG = GlShaderLoader::class.java.simpleName

    const val GL_CREATE_PROGRAM = "glCreateProgram"
    const val GL_ATTACH_SHADER = "glAttachShader"

    var assetManager: AssetManager? = null

    fun createProgram(name: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, name)
        if (vertexShader == 0) {
            return 0
        }
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, name)
        if (fragmentShader == 0) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        checkGlError(GL_CREATE_PROGRAM)
        if (program == 0) {
            Log.e(TAG, "Could not create program")
        }
        GLES20.glAttachShader(program, vertexShader)
        checkGlError(GL_ATTACH_SHADER)
        GLES20.glAttachShader(program, fragmentShader)
        checkGlError(GL_ATTACH_SHADER)
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ")
            Log.e(TAG, GLES20.glGetProgramInfoLog(program))
            GLES20.glDeleteProgram(program)
            program = 0
        }
        return program
    }

    fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            Log.e(TAG, msg)
            throw RuntimeException(msg)
        }
    }

    fun createFloatBuffer(array: FloatArray): FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(array.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        return buffer.asFloatBuffer().apply {
            put(array)
            position(0)
        }
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
        checkGlError("glCreateShader type=$shaderType")
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
}
