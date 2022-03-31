#version 330 core

in vec3 fragmentPos;
in vec3 faceNormal;
in vec3 color;

// MRT - Multiple Render Targets
layout (location = 0) out vec3 gPos;
layout (location = 1) out vec3 gNor;
layout (location = 2) out vec3 gCol;

void main()
{
    gPos = fragmentPos;
    gNor = faceNormal;
    gCol = color;
}
