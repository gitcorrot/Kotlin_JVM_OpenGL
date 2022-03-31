#version 330 core

layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inCol;

out VS_OUT {
    vec3 _fragmentPos;
    mat4 _projection;
} vs_out;

uniform mat4 m;
uniform mat4 v;
uniform mat4 p;

void main()
{
    gl_Position = vec4(inPos.xyz, 1.0f);

    vs_out._fragmentPos = vec3(m * vec4(inPos.xyz, 1.0f));
    vs_out._projection = p * v * m;
}
