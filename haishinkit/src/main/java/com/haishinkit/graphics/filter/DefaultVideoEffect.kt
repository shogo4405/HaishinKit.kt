package com.haishinkit.graphics.filter

class DefaultVideoEffect(
    override val name: String = "default"
) : VideoEffect {

    companion object {
        val shared = DefaultVideoEffect()
    }
}
