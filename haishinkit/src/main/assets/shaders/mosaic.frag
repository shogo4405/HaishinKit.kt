#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2 vTexcoord;
uniform samplerExternalOES uTexture;
uniform float mosaicScale;

void main() {
    vec2 uv = vTexcoord;
    uv = floor(uv * mosaicScale) / mosaicScale;
    vec4 color = texture2D(uTexture, uv);
    gl_FragColor = vec4(color.rgb, 1.0);
}
