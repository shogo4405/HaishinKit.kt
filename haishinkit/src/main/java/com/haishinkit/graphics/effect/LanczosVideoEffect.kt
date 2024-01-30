package com.haishinkit.graphics.effect

import com.haishinkit.graphics.glsl.ShaderStage
import com.haishinkit.graphics.glsl.Uniform

/**
 * An object that provides a resampling filter by lanczos algorithm.
 */
class LanczosVideoEffect(
    override val name: String = "lanczos",
) : VideoEffect {
    @Uniform(binding = 0, shaderStage = ShaderStage.VERTEX)
    var texelWidth = 1f

    @Uniform(binding = 1, shaderStage = ShaderStage.VERTEX)
    var texelHeight = 1f
}
