package com.haishinkit.graphics.effect

data class DefaultVideoEffect private constructor(
    override val name: String = "default"
) : VideoEffect {

    companion object {
        val shared = DefaultVideoEffect()
    }
}
