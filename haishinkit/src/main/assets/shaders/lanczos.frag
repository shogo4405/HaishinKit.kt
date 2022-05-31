#extension GL_OES_EGL_image_external : require

uniform samplerExternalOES uTexture;

varying vec2 centerTextureCoordinate;
varying vec2 oneStepLeftTextureCoordinate;
varying vec2 twoStepsLeftTextureCoordinate;
varying vec2 threeStepsLeftTextureCoordinate;
varying vec2 fourStepsLeftTextureCoordinate;
varying vec2 oneStepRightTextureCoordinate;
varying vec2 twoStepsRightTextureCoordinate;
varying vec2 threeStepsRightTextureCoordinate;
varying vec2 fourStepsRightTextureCoordinate;

void main() {
    vec4 fragmentColor = texture2D(uTexture, centerTextureCoordinate) * 0.38026;

    fragmentColor += texture2D(uTexture, oneStepLeftTextureCoordinate) * 0.27667;
    fragmentColor += texture2D(uTexture, oneStepRightTextureCoordinate) * 0.27667;

    fragmentColor += texture2D(uTexture, twoStepsLeftTextureCoordinate) * 0.08074;
    fragmentColor += texture2D(uTexture, twoStepsRightTextureCoordinate) * 0.08074;

    fragmentColor += texture2D(uTexture, threeStepsLeftTextureCoordinate) * -0.02612;
    fragmentColor += texture2D(uTexture, threeStepsRightTextureCoordinate) * -0.02612;

    fragmentColor += texture2D(uTexture, fourStepsLeftTextureCoordinate) * -0.02143;
    fragmentColor += texture2D(uTexture, fourStepsRightTextureCoordinate) * -0.02143;

    gl_FragColor = fragmentColor;
}
