#version 330 core

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inCol;
layout (location = 2) in vec2 inTex;

out vec3 color;
out vec2 texturePos;

void main()
{
    gl_Position = vec4(inPos.xyz, 1.0f);
    color = inCol;
    texturePos = inTex;
}
