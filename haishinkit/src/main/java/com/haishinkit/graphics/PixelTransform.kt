package com.haishinkit.graphics

import android.content.Context
import android.util.Size
import android.view.Surface
import com.haishinkit.gles.ThreadPixelTransform
import com.haishinkit.graphics.effect.VideoEffect
import com.haishinkit.screen.Screen
import kotlin.reflect.KClass

/**
 * The PixelTransform interface provides some graphics operations.
 */
interface PixelTransform {
    val applicationContext: Context

    /**
     * Specifies the off screen object.
     */
    var screen: Screen?

    /**
     * Specifies the surface that is an output source.
     */
    var surface: Surface?

    /**
     * Specifies the current width and height of the output surface.
     */
    var imageExtent: Size

    /**
     * Specifies the videoEffect such as a monochrome, a sepia.
     */
    var videoEffect: VideoEffect

    /**
     * Specifies the videoGravity how the displays the inputSurface's visual content.
     */
    var videoGravity: VideoGravity

    /**
     * Specifies the frameRate for an output source in frames/sec.
     */
    var frameRate: Int

    companion object {
        private var pixelTransforms: MutableList<KClass<*>> = mutableListOf()

        fun create(applicationContext: Context): PixelTransform {
            if (pixelTransforms.isEmpty()) {
                return ThreadPixelTransform(applicationContext)
            }
            return try {
                val pixelTransformClass = pixelTransforms.first()
                pixelTransformClass.java.newInstance() as PixelTransform
            } catch (e: Exception) {
                return ThreadPixelTransform(applicationContext)
            }
        }

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
