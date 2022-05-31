package com.haishinkit.graphics.filter

data class DefaultVideoEffect private constructor(
    override val name: String = "default"
) : VideoEffect {

    companion object {
        val shared = DefaultVideoEffect()
    }
}
