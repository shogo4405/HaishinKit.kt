package com.haishinkit.graphics.filter

internal class DefaultVideoEffect(
    override val name: String = "default"
) : VideoEffect {

    companion object {
        val shared = DefaultVideoEffect()
    }
}
