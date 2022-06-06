package com.haishinkit.graphics.effect

import com.haishinkit.graphics.glsl.ShaderStage
import com.haishinkit.graphics.glsl.Uniform

/**
 * An object that provides a resampling filter by lanczos algorithm.
 */
class LanczosVideoEffect(
    override val name: String = "lanczos"
) : VideoEffect {
    @Uniform(name = "uTexelWidth", binding = 0, shaderStage = ShaderStage.VERTEX)
    var texelWidth = 1f

    @Uniform(name = "uTexelHeight", binding = 1, shaderStage = ShaderStage.VERTEX)
    var texelHeight = 1f
}
