#version 330 core

layout (location = 0) in vec3 inPos;

out vec3 texturePos;

uniform mat4 p;
uniform mat4 v;

void main()
{
    texturePos = inPos;
    gl_Position = (p * v * vec4(inPos.xyz, 1.0f)).xyww;
}
