#version 330 core

in vec3 fragmentPos;
in vec3 modelNormal;
in vec2 texturePos;

// MRT - Multiple Render Targets
layout (location = 0) out vec3 gPos;
layout (location = 1) out vec3 gNor;
layout (location = 2) out vec3 gCol;

uniform sampler2D colorPaletteTexture;

void main()
{
    gPos = fragmentPos;
    gNor = modelNormal;
    gCol = texture(colorPaletteTexture, texturePos).rgb;
    //    gCol = vec3(0.5, 0.5, 1.0);
//    gCol = vec4(vec3(gl_FragCoord.z), 1.0);
}
