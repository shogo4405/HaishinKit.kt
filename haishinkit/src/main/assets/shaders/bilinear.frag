#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;
in vec2 vTexcoord;
out vec4 fragColor;
uniform samplerExternalOES uTexture;

vec4 textureBilinear(sampler2D tex, vec2 texCoords) {
    vec2 textureSize = vec2(textureSize(tex, 0));
    vec2 invTextureSize = 1.0 / textureSize;

    vec2 texel = texCoords * textureSize;
    vec2 i = floor(texel);
    vec2 f = fract(texel);

    if (f.x > 0.5) {
        i.x += 1.0;
        f.x -= 0.5;
    } else {
        f.x += 0.5;
    }
    if (f.y > 0.5) {
        i.y += 1.0;
        f.y -= 0.5;
    } else {
        f.y += 0.5;
    }

    vec4 a = texture(tex, (i + vec2(-0.5, -0.5)) * invTextureSize);
    vec4 b = texture(tex, (i + vec2(0.5, -0.5)) * invTextureSize);
    vec4 c = texture(tex, (i + vec2(-0.5, 0.5)) * invTextureSize);
    vec4 d = texture(tex, (i + vec2(0.5, 0.5)) * invTextureSize);

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

void main() {
    fragColor = textureBilinear(uTexture, vTexcoord);
}

