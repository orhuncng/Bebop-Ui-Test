#extension GL_OES_EGL_image_external : require

precision mediump float;
uniform samplerExternalOES uTexture;
uniform float uAlpha;
varying vec2 vTexCoords;

void main()
{
    gl_FragColor.xyz = texture2D(uTexture, vTexCoords).xyz;
    gl_FragColor.a = uAlpha;
}