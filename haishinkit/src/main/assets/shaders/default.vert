uniform mediump mat4 uMVPMatrix;
attribute mediump vec4 aPosition;
attribute mediump vec2 aTexcoord;
varying mediump vec2 vTexcoord;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vTexcoord = aTexcoord;
}
