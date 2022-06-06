package com.haishinkit.graphics.glsl

annotation class Uniform(
    val name: String,
    val binding: Int = 0,
    val shaderStage: ShaderStage = ShaderStage.ALL
)
