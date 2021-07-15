#version 330 core

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec2 inTex;

out vec2 texturePos;

void main()
{
    gl_Position = vec4(inPos.xyz, 1.0);
//    gl_Position = vec4(inPos.x, inPos.y, 1.0, 1.0);
    texturePos = inTex;
}
