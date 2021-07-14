#version 330 core

in vec3 fragmentPos2;
in vec3 modelNormal2;
in vec3 color2;

// MRT - Multiple Render Targets
layout (location = 0) out vec3 gPos;
layout (location = 1) out vec3 gNor;
layout (location = 2) out vec3 gCol;

void main()
{
    gPos = fragmentPos2;
    gNor = modelNormal2;
    gCol = color2;
}
