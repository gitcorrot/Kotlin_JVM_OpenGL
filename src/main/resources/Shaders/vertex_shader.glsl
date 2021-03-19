#version 330 core

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inNorm;
layout (location = 2) in vec2 inTex;

out vec2 texturePos;

uniform mat4 mvp;

void main()
{
    gl_Position = mvp * vec4(inPos, 1.0f);
    texturePos = inTex;
}
