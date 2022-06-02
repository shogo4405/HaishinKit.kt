package com.haishinkit.graphics.effect

/**
 * An object that provides a no effect.
 */
class DefaultVideoEffect private constructor(
    override val name: String = "default"
) : VideoEffect {

    companion object {
        val shared = DefaultVideoEffect()
    }
}
