#version 450

layout (location = 0) in vec4 pos;
layout (location = 1) in vec2 attr;
layout (location = 0) out vec2 texcoord;

void main() {
    gl_Position = pos;
    texcoord = attr;
}
