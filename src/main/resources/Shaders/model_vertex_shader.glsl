#version 330 core

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inNorm;
layout (location = 2) in vec2 inTex;

out vec3 fragmentPos;
out vec3 modelNormal;
out vec2 texturePos;

uniform mat4 m;
uniform mat4 v;
uniform mat4 p;

void main()
{
    gl_Position = p * v * m * vec4(inPos.xyz, 1.0f);

    fragmentPos = vec3(m * vec4(inPos.xyz, 1.0f));

    // apply transformations to normal and eliminate scaling problem
    modelNormal = normalize(mat3(transpose(inverse(m))) * inNorm);
    texturePos = inTex;
}
