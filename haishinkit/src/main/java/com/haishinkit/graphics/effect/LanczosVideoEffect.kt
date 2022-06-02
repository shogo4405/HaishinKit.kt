package com.haishinkit.graphics.effect

import com.haishinkit.graphics.glsl.Uniform

/**
 * An object that provides a resampling filter by lanczos algorithm.
 */
class LanczosVideoEffect(
    override val name: String = "lanczos"
) : VideoEffect {
    @Uniform(name = "uTexelWidth")
    var texelWidth = 1f

    @Uniform(name = "uTexelHeight")
    var texelHeight = 1f
}
