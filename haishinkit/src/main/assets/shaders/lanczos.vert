uniform mediump mat4 uMVPMatrix;

attribute mediump vec4 aPosition;
attribute mediump vec2 aTexcoord;

uniform mediump float uTexelWidth;
uniform mediump float uTexelHeight;

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
    gl_Position = uMVPMatrix * aPosition;

    float texelWidthOffset = 1.0 / uTexelWidth;
    float texelHeightOffset = 1.0 / uTexelHeight;

    vec2 firstOffset = vec2(texelWidthOffset, texelHeightOffset);
    vec2 secondOffset = vec2(2.0 * texelWidthOffset, 2.0 * texelHeightOffset);
    vec2 thirdOffset = vec2(3.0 * texelWidthOffset, 3.0 * texelHeightOffset);
    vec2 fourthOffset = vec2(4.0 * texelWidthOffset, 4.0 * texelHeightOffset);

    centerTextureCoordinate = aTexcoord;
    oneStepLeftTextureCoordinate = aTexcoord - firstOffset;
    twoStepsLeftTextureCoordinate = aTexcoord - secondOffset;
    threeStepsLeftTextureCoordinate = aTexcoord - thirdOffset;
    fourStepsLeftTextureCoordinate = aTexcoord - fourthOffset;
    oneStepRightTextureCoordinate = aTexcoord + firstOffset;
    twoStepsRightTextureCoordinate = aTexcoord + secondOffset;
    threeStepsRightTextureCoordinate = aTexcoord + thirdOffset;
    fourStepsRightTextureCoordinate = aTexcoord + fourthOffset;
}