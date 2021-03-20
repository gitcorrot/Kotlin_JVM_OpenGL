#version 330 core

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inNorm;
layout (location = 2) in vec2 inTex;

out vec3 position;
out vec3 normal;
out vec2 texturePos;

uniform mat4 mvp;

void main()
{
    gl_Position = mvp * vec4(inPos.xyz, 1.0f);
    position = inPos; // TODO: multiply by model matrix in case of translation/rotation
    normal = normalize(inNorm);
    texturePos = inTex;
}
