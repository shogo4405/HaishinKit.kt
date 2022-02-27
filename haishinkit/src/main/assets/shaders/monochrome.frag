#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 texcoordVarying;
uniform samplerExternalOES texture;
const vec3 monochromeScale = vec3(0.298912, 0.586611, 0.114478);
void main() {
    vec4 color = texture2D(texture, texcoordVarying);
    float grayColor = dot(color.rgb, monochromeScale);
    color = vec4(vec3(grayColor), 1.0);
    gl_FragColor = vec4(color);
}
