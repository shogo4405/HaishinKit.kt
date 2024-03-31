package com.haishinkit.graphics.effect

import com.haishinkit.graphics.glsl.RequirementsDirective
import com.haishinkit.graphics.glsl.VersionCode

/**
 * An object that provides a resampling filter by bilinear algorithm.
 */
@RequirementsDirective(VersionCode.ES300)
class BilinearVideoEffect(
    override val name: String = "bilinear"
) : VideoEffect
