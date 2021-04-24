#version 330 core

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec2 inTex;

out vec2 texturePos;

uniform mat4 m;
uniform mat4 v;
uniform mat4 p;

void main()
{
    gl_Position = p * v * m * vec4(inPos.xyz, 1.0f);
    texturePos = inTex;
}
