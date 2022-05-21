#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 vTexcoord;
uniform samplerExternalOES uTexture;
const vec3 cMonochromeScale = vec3(0.298912, 0.586611, 0.114478);

void main() {
    vec4 color = texture2D(uTexture, vTexcoord);
    float grayColor = dot(color.rgb, cMonochromeScale);
    color = vec4(vec3(grayColor), 1.0);
    gl_FragColor = vec4(color);
}
