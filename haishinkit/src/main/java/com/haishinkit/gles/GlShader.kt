package com.haishinkit.gles

object GlShader {
    const val VERTEX: String = """attribute vec4 position;
attribute vec2 texcoord;
varying vec2 texcoordVarying;
void main() {
  gl_Position = position;
  texcoordVarying = texcoord;
}"""

    const val FRAGMENT: String = """#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 texcoordVarying;
uniform samplerExternalOES texture;
void main() {
  gl_FragColor = texture2D(texture, texcoordVarying);
}"""
}
