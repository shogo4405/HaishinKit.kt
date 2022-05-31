package com.haishinkit.graphics.gles

import android.opengl.GLES20
import com.haishinkit.graphics.effect.VideoEffect
import java.lang.reflect.Method

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
                (value is Float) -> GLES20.glUniform1f(handler.key, value)
                else -> {}
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
