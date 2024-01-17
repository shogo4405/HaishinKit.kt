package com.haishinkit.gles

import android.opengl.GLES20
import android.util.Log
import com.haishinkit.graphics.effect.VideoEffect
import java.lang.reflect.Method
import java.nio.FloatBuffer
import java.nio.IntBuffer

internal data class Program(
    val id: Int = INVALID_VALUE,
    val vertexShader: Int = INVALID_VALUE,
    val fragmentShaper: Int = INVALID_VALUE,
    val positionHandle: Int = INVALID_VALUE,
    val texCoordHandle: Int = INVALID_VALUE,
    val textureHandle: Int = INVALID_VALUE,
    val mvpMatrixHandle: Int = INVALID_VALUE,
    private val handlers: Map<Int, Method>
) {
    companion object {
        private const val INVALID_VALUE = 0
        private const val TAG = "Program"
    }

    init {
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
    }

    fun bind(visualEffect: VideoEffect) {
        for (handler in handlers) {
            val value = handler.value.invoke(visualEffect)
            when (true) {
                (value is Int) -> GLES20.glUniform1i(handler.key, value)
                (value is IntBuffer) -> {
                    when (value.remaining()) {
                        1 -> GLES20.glUniform1iv(handler.key, 1, value)
                        2 -> GLES20.glUniform2iv(handler.key, 1, value)
                        3 -> GLES20.glUniform3iv(handler.key, 1, value)
                        4 -> GLES20.glUniform4iv(handler.key, 1, value)
                    }
                }

                (value is Float) -> GLES20.glUniform1f(handler.key, value)
                (value is FloatBuffer) -> {
                    when (value.remaining()) {
                        1 -> GLES20.glUniform1fv(handler.key, 1, value)
                        2 -> GLES20.glUniform2fv(handler.key, 1, value)
                        3 -> GLES20.glUniform3fv(handler.key, 1, value)
                        4 -> GLES20.glUniform4fv(handler.key, 1, value)
                    }
                }

                else -> {
                    Log.e(TAG, "value type not supported")
                }
            }
        }
    }

    fun dispose() {
        GLES20.glDetachShader(id, vertexShader)
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDetachShader(id, fragmentShaper)
        GLES20.glDeleteShader(fragmentShaper)
        GLES20.glDeleteProgram(id)
    }
}
