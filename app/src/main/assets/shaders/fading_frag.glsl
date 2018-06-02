#ifdef GL_ES
#define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float center;
uniform float alphaRadius;

void main()
{
    gl_FragColor = v_color * texture2D(u_texture, v_texCoords);

    float distance = 2.0 * (center - v_texCoords.y) * (center - v_texCoords.y) /
                     (alphaRadius * alphaRadius);

    gl_FragColor.a = gl_FragColor.a * (1.0 - distance);
}