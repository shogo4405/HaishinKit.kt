precision mediump float;
varying vec2 vTexcoord;
uniform sampler2D uTexture;

void main() {
    vec4 color = texture2D(uTexture, vTexcoord);
    float v = color.x * 0.298912 + color.y * 0.586611 + color.z * 0.114478;
    color.x = v * 0.9;
    color.y = v * 0.7;
    color.z = v * 0.4;
    gl_FragColor = vec4(color);
}
