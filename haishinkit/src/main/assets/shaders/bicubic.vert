#version 300 es

uniform mediump mat4 uMVPMatrix;
in mediump vec4 aPosition;
in mediump vec2 aTexcoord;
out mediump vec2 vTexcoord;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vTexcoord = aTexcoord;
}
