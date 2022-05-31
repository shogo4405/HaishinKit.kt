package com.haishinkit.graphics.effect

import com.haishinkit.graphics.glsl.Layout

data class LanczosVideoEffect(
    override val name: String = "lanczos"
) : VideoEffect {
    @Layout(name = "uTexelWidth")
    var texelWidth = 1f

    @Layout(name = "uTexelHeight")
    var texelHeight = 1f
}
