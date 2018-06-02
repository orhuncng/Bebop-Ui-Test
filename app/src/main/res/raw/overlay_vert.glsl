uniform mat4 uMvpMatrix;
attribute vec3 aPosition;
attribute vec2 aTexCoords;
varying vec2 vTexCoords;

void main()
{
    gl_Position = vec4(aPosition, 1);
    vTexCoords = aTexCoords;
}