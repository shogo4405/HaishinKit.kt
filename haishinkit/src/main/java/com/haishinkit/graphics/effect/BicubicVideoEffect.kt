package com.haishinkit.graphics.effect

import com.haishinkit.graphics.glsl.RequirementsDirective
import com.haishinkit.graphics.glsl.VersionCode

/**
 * An object that provides a resampling filter by bicubic algorithm.
 */
@RequirementsDirective(VersionCode.ES300)
class BicubicVideoEffect(
    override val name: String = "bicubic"
) : VideoEffect
