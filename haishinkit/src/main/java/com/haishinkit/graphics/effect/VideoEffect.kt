package com.haishinkit.graphics.effect

import com.haishinkit.graphics.glsl.Uniform
import java.lang.reflect.Method

/**
 * The VideoEffect interface provides to create a custom video effect.
 */
interface VideoEffect {
    /**
     * The name of shader.
     */
    val name: String

    val uniforms: Array<Uniform>
        get() {
            val values = mutableListOf<Uniform>()
            for (method in javaClass.methods) {
                val uniform = method.getAnnotation(Uniform::class.java) ?: continue
                values.add(uniform)
            }
            values.sortBy { uniform -> uniform.binding }
            return values.toTypedArray()
        }

    val methods: Array<Method>
        get() {
            val values = mutableMapOf<Int, Method>()
            for (method in javaClass.methods) {
                val uniform = method.getAnnotation(Uniform::class.java) ?: continue
                values[uniform.binding] = javaClass.getMethod(method.name.split("$")[0])
            }
            return MutableList(values.size) { index -> values[index]!! }.toTypedArray()
        }
}
