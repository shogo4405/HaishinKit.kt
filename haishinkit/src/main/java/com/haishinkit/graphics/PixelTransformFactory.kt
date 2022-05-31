package com.haishinkit.graphics

import com.haishinkit.graphics.gles.GlThreadPixelTransform
import kotlin.reflect.KClass

class PixelTransformFactory {
    fun create(): PixelTransform {
        if (pixelTransforms.isEmpty()) {
            return GlThreadPixelTransform()
        }
        return try {
            val pixelTransformClass = pixelTransforms.first()
            pixelTransformClass.java.newInstance() as PixelTransform
        } catch (e: Exception) {
            return GlThreadPixelTransform()
        }
    }

    companion object {
        private var pixelTransforms: MutableList<KClass<*>> = mutableListOf()

        fun <T : PixelTransform> registerPixelTransform(clazz: KClass<T>) {
            for (i in 0 until pixelTransforms.size) {
                if (pixelTransforms[i] == clazz) {
                    return
                }
            }
            pixelTransforms.add(clazz)
        }

        fun <T : PixelTransform> unregisterPixelTransform(clazz: KClass<T>) {
            for (i in (0 until pixelTransforms.size).reversed()) {
                if (pixelTransforms[i] == clazz) {
                    pixelTransforms.removeAt(i)
                }
            }
        }
    }
}
